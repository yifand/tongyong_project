package com.vdc.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdc.platform.common.ApiResult;
import com.vdc.platform.dto.ChannelRequest;
import com.vdc.platform.dto.EdgeBoxRequest;
import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IAlarmService;
import com.vdc.platform.service.IChannelService;
import com.vdc.platform.service.IEdgeBoxService;
import com.vdc.platform.service.IOperationLogService;
import com.vdc.platform.service.IWorkSessionService;
import com.vdc.platform.service.impl.DeviceRemoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final IEdgeBoxService edgeBoxService;
    private final IChannelService channelService;
    private final IOperationLogService operationLogService;
    private final IAlarmService alarmService;
    private final IWorkSessionService workSessionService;
    private final DeviceRemoteService deviceRemoteService;

    @GetMapping("/boxes")
    @PreAuthorize("hasAuthority('device:read') or hasAuthority('admin')")
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<EdgeBox>> listBoxes(@ParameterObject com.baomidou.mybatisplus.extension.plugins.pagination.Page<EdgeBox> page,
                                                                                     @RequestParam(required = false) Long siteId,
                                                                                     @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<EdgeBox> wrapper = new LambdaQueryWrapper<>();
        if (siteId != null) {
            wrapper.eq(EdgeBox::getSiteId, siteId);
        }
        if (status != null) {
            wrapper.eq(EdgeBox::getStatus, status);
        }
        wrapper.orderByDesc(EdgeBox::getCreatedAt);
        return ApiResult.success(edgeBoxService.page(page, wrapper));
    }

    @PostMapping("/boxes")
    @PreAuthorize("hasAuthority('device:write') or hasAuthority('admin')")
    public ApiResult<Void> createBox(@Valid @RequestBody EdgeBoxRequest request, HttpServletRequest httpRequest) {
        EdgeBox existing = edgeBoxService.lambdaQuery().eq(EdgeBox::getBoxId, request.getBoxId()).one();
        if (existing != null) {
            return ApiResult.error(400, "Box ID already exists");
        }
        EdgeBox box = new EdgeBox();
        box.setBoxId(request.getBoxId());
        box.setBoxName(request.getBoxName());
        box.setSiteId(request.getSiteId());
        box.setIpAddress(request.getIpAddress());
        box.setSecretKey(UUID.randomUUID().toString());
        box.setVersion(request.getVersion());
        box.setStatus(0);
        edgeBoxService.save(box);
        recordLog(getCurrentUser(), "CREATE_BOX", "Created box: " + request.getBoxId(), 1, httpRequest);
        return ApiResult.success();
    }

    @DeleteMapping("/boxes/{id}")
    @PreAuthorize("hasAuthority('device:write') or hasAuthority('admin')")
    public ApiResult<Void> deleteBox(@PathVariable Long id, HttpServletRequest httpRequest) {
        EdgeBox box = edgeBoxService.getById(id);
        if (box == null) {
            return ApiResult.error(404, "Box not found");
        }
        channelService.lambdaUpdate().eq(Channel::getBoxId, id).remove();
        edgeBoxService.removeById(id);
        recordLog(getCurrentUser(), "DELETE_BOX", "Deleted box: " + id, 1, httpRequest);
        return ApiResult.success();
    }

    @PostMapping("/boxes/{id}/reboot")
    @PreAuthorize("hasAuthority('device:write') or hasAuthority('admin')")
    public ApiResult<Void> rebootBox(@PathVariable Long id, HttpServletRequest httpRequest) {
        EdgeBox box = edgeBoxService.getById(id);
        if (box == null) {
            return ApiResult.error(404, "Box not found");
        }
        if (box.getIpAddress() == null || box.getIpAddress().isBlank()) {
            return ApiResult.error(400, "Box IP address not configured");
        }
        try {
            ResponseEntity<String> response = deviceRemoteService.rebootBox(box.getIpAddress());
            if (response.getStatusCode().is2xxSuccessful()) {
                recordLog(getCurrentUser(), "REBOOT_BOX", "Rebooted box: " + id, 1, httpRequest);
                return ApiResult.success();
            }
            return ApiResult.error(502, "Box reboot failed: " + response.getStatusCode());
        } catch (Exception e) {
            return ApiResult.error(502, "Box reboot failed: " + e.getMessage());
        }
    }

    @PostMapping("/channels")
    @PreAuthorize("hasAuthority('device:write') or hasAuthority('admin')")
    public ApiResult<Void> createChannel(@Valid @RequestBody ChannelRequest request,
                                          HttpServletRequest httpRequest) {
        EdgeBox box = edgeBoxService.getById(request.getBoxId());
        if (box == null) {
            return ApiResult.error(400, "Box not found");
        }
        Channel existing = channelService.lambdaQuery()
                .eq(Channel::getBoxId, request.getBoxId())
                .eq(Channel::getChannelId, request.getChannelId())
                .one();
        if (existing != null) {
            return ApiResult.error(400, "Channel ID already exists in this box");
        }
        Channel channel = new Channel();
        channel.setChannelId(request.getChannelId());
        channel.setChannelName(request.getChannelName());
        channel.setBoxId(request.getBoxId());
        channel.setChannelType(request.getChannelType());
        channel.setAlgorithmType(request.getAlgorithmType());
        channel.setRtspUrl(request.getRtspUrl());
        channel.setUsername(request.getUsername());
        channel.setPassword(request.getPassword());
        channel.setStatus(0);
        channelService.save(channel);

        if (box.getIpAddress() != null && !box.getIpAddress().isBlank()) {
            try {
                ResponseEntity<String> response = deviceRemoteService.syncChannelConfig(box.getIpAddress(), channel);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.warn("Channel config sync failed for box {}: {}", box.getIpAddress(), response.getStatusCode());
                }
            } catch (Exception e) {
                log.warn("Channel config sync failed for box {}: {}", box.getIpAddress(), e.getMessage());
            }
        }

        recordLog(getCurrentUser(), "CREATE_CHANNEL", "Created channel: " + request.getChannelId(), 1, httpRequest);
        return ApiResult.success();
    }

    @GetMapping("/channels")
    @PreAuthorize("hasAuthority('device:read') or hasAuthority('admin')")
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<Channel>> listChannels(@ParameterObject com.baomidou.mybatisplus.extension.plugins.pagination.Page<Channel> page,
                                                                                     @RequestParam(required = false) Long boxId,
                                                                                     @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        if (boxId != null) {
            wrapper.eq(Channel::getBoxId, boxId);
        }
        if (status != null) {
            wrapper.eq(Channel::getStatus, status);
        }
        wrapper.orderByDesc(Channel::getCreatedAt);
        return ApiResult.success(channelService.page(page, wrapper));
    }

    @PutMapping("/channels/{id}")
    @PreAuthorize("hasAuthority('device:write') or hasAuthority('admin')")
    public ApiResult<Void> updateChannel(@PathVariable Long id, @Valid @RequestBody ChannelRequest request,
                                          HttpServletRequest httpRequest) {
        Channel channel = channelService.getById(id);
        if (channel == null) {
            return ApiResult.error(404, "Channel not found");
        }
        channel.setChannelId(request.getChannelId());
        channel.setChannelName(request.getChannelName());
        channel.setBoxId(request.getBoxId());
        channel.setChannelType(request.getChannelType());
        channel.setAlgorithmType(request.getAlgorithmType());
        channel.setRtspUrl(request.getRtspUrl());
        channel.setUsername(request.getUsername());
        channel.setPassword(request.getPassword());
        channelService.updateById(channel);

        EdgeBox box = edgeBoxService.getById(channel.getBoxId());
        if (box != null && box.getIpAddress() != null && !box.getIpAddress().isBlank()) {
            try {
                ResponseEntity<String> response = deviceRemoteService.syncChannelConfig(box.getIpAddress(), channel);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.warn("Channel config sync failed for box {}: {}", box.getIpAddress(), response.getStatusCode());
                }
            } catch (Exception e) {
                log.warn("Channel config sync failed for box {}: {}", box.getIpAddress(), e.getMessage());
            }
        }

        recordLog(getCurrentUser(), "UPDATE_CHANNEL", "Updated channel: " + id, 1, httpRequest);
        return ApiResult.success();
    }

    @GetMapping("/channels/{id}/preview")
    @PreAuthorize("hasAuthority('device:read') or hasAuthority('admin')")
    public ApiResult<Map<String, Object>> getChannelPreview(@PathVariable Long id) {
        Channel channel = channelService.getById(id);
        if (channel == null) {
            return ApiResult.error(404, "Channel not found");
        }
        EdgeBox box = edgeBoxService.getById(channel.getBoxId());
        if (box == null || box.getIpAddress() == null || box.getIpAddress().isBlank()) {
            return ApiResult.error(400, "Box IP address not configured");
        }
        try {
            ResponseEntity<Map> response = deviceRemoteService.getStreamPreview(box.getIpAddress(), channel.getChannelId());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("streamUrl", response.getBody().get("streamUrl"));
                return ApiResult.success(result);
            }
            return ApiResult.error(502, "Preview request failed: " + response.getStatusCode());
        } catch (Exception e) {
            return ApiResult.error(502, "Preview request failed: " + e.getMessage());
        }
    }

    @GetMapping("/monitor")
    @PreAuthorize("hasAuthority('device:read') or hasAuthority('admin')")
    public ApiResult<Map<String, Object>> monitor() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> boxStats = ((com.vdc.platform.mapper.EdgeBoxMapper) edgeBoxService.getBaseMapper()).selectBoxStats();
        List<Map<String, Object>> channelStats = ((com.vdc.platform.mapper.EdgeBoxMapper) edgeBoxService.getBaseMapper()).selectChannelStats();

        long onlineBoxes = 0L;
        long offlineBoxes = 0L;
        Map<Long, Map<String, Long>> siteMap = new HashMap<>();
        for (Map<String, Object> row : boxStats) {
            Long siteId = ((Number) row.get("site_id")).longValue();
            Integer status = ((Number) row.get("status")).intValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            siteMap.computeIfAbsent(siteId, k -> new HashMap<>()).put(status == 1 ? "onlineBoxes" : "offlineBoxes", cnt);
            if (status == 1) {
                onlineBoxes += cnt;
            } else {
                offlineBoxes += cnt;
            }
        }
        for (Map<String, Object> row : channelStats) {
            Long siteId = ((Number) row.get("site_id")).longValue();
            Integer status = ((Number) row.get("status")).intValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            siteMap.computeIfAbsent(siteId, k -> new HashMap<>()).put(status == 1 ? "onlineChannels" : "offlineChannels", cnt);
        }

        long todayAlarms = ((com.vdc.platform.mapper.AlarmMapper) alarmService.getBaseMapper()).selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.vdc.platform.entity.Alarm>()
                        .ge(com.vdc.platform.entity.Alarm::getAlarmTime, java.time.LocalDate.now().atStartOfDay())
        );

        long todaySessions = ((com.vdc.platform.mapper.WorkSessionMapper) workSessionService.getBaseMapper()).selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.vdc.platform.entity.WorkSession>()
                        .ge(com.vdc.platform.entity.WorkSession::getStartTime, java.time.LocalDate.now().atStartOfDay())
        );

        List<Long> alarmTrend = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            java.time.LocalDateTime start = java.time.LocalDate.now().atStartOfDay().plusHours(i);
            java.time.LocalDateTime end = start.plusHours(1);
            long cnt = ((com.vdc.platform.mapper.AlarmMapper) alarmService.getBaseMapper()).selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.vdc.platform.entity.Alarm>()
                            .ge(com.vdc.platform.entity.Alarm::getAlarmTime, start)
                            .lt(com.vdc.platform.entity.Alarm::getAlarmTime, end)
            );
            alarmTrend.add(cnt);
        }

        result.put("onlineBoxes", onlineBoxes);
        result.put("offlineBoxes", offlineBoxes);
        result.put("todayAlarms", todayAlarms);
        result.put("todaySessions", todaySessions);
        result.put("alarmTrend", alarmTrend);
        result.put("siteStats", siteMap);
        return ApiResult.success(result);
    }

    private SecurityUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (SecurityUser) principal;
    }

    private void recordLog(SecurityUser user, String type, String content, int result, HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setUserId(user.getUserId());
        log.setUsername(user.getUsername());
        log.setIpAddress(getClientIp(request));
        log.setOperationType(type);
        log.setOperationContent(content);
        log.setResult(result);
        log.setCreatedAt(LocalDateTime.now());
        operationLogService.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
