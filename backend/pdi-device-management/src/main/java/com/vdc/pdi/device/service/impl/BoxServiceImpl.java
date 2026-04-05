package com.vdc.pdi.device.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.common.enums.ResultCode;
import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.device.config.DeviceConfig;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.entity.QEdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import com.vdc.pdi.device.domain.vo.BoxStatus;
import com.vdc.pdi.device.domain.vo.HeartbeatInfo;
import com.vdc.pdi.device.dto.request.BoxQueryRequest;
import com.vdc.pdi.device.dto.request.BoxRequest;
import com.vdc.pdi.device.dto.response.BoxResponse;
import com.vdc.pdi.device.dto.response.DeviceMetricsResponse;
import com.vdc.pdi.device.mapper.BoxMapper;
import com.vdc.pdi.device.service.BoxService;
import com.vdc.pdi.device.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 盒子服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoxServiceImpl implements BoxService {

    private final EdgeBoxRepository edgeBoxRepository;
    private final BoxMapper boxMapper;
    private final DeviceConfig deviceConfig;
    private final ChannelService channelService;

    @Override
    public PageResponse<BoxResponse> listBoxes(BoxQueryRequest request, Long currentSiteId) {
        QEdgeBox qBox = QEdgeBox.edgeBox;
        BooleanBuilder predicate = new BooleanBuilder();

        // 站点过滤
        if (request.getSiteId() != null) {
            predicate.and(qBox.siteId.eq(request.getSiteId()));
        } else if (currentSiteId != null) {
            predicate.and(qBox.siteId.eq(currentSiteId));
        }

        // 状态过滤
        if (request.getStatus() != null) {
            predicate.and(qBox.status.eq(request.getStatus()));
        }

        // 关键字搜索（名称或IP）
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword();
            predicate.and(qBox.name.containsIgnoreCase(keyword)
                    .or(qBox.ipAddress.containsIgnoreCase(keyword)));
        }

        // 未删除
        predicate.and(qBox.deletedAt.isNull());

        // 分页
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<EdgeBox> boxPage = edgeBoxRepository.findAll(predicate, pageable);

        List<BoxResponse> responses = boxMapper.toResponseList(boxPage.getContent());
        // 这里可以设置站点名称，如果有站点服务的话

        return PageResponse.of(responses, boxPage.getTotalElements(), request.getPage(), size);
    }

    @Override
    public BoxResponse getBox(Long id, Long currentSiteId) {
        EdgeBox box = getBoxAndCheckPermission(id, currentSiteId);
        BoxResponse response = boxMapper.toResponse(box);
        // 设置站点名称
        return response;
    }

    @Override
    @Transactional
    public Long createBox(BoxRequest request, Long currentSiteId, Long currentUserId) {
        // 检查IP地址是否已存在
        Optional<EdgeBox> existingBox = edgeBoxRepository.findByIpAddressAndDeletedAtIsNull(request.getIpAddress());
        if (existingBox.isPresent()) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "IP地址已存在");
        }

        EdgeBox box = boxMapper.toEntity(request);
        box.setStatus(0); // 离线
        box.setCreatedBy(currentUserId);

        EdgeBox savedBox = edgeBoxRepository.save(box);
        log.info("创建盒子成功: id={}, name={}", savedBox.getId(), savedBox.getName());

        return savedBox.getId();
    }

    @Override
    @Transactional
    public void updateBox(Long id, BoxRequest request, Long currentSiteId) {
        EdgeBox box = getBoxAndCheckPermission(id, currentSiteId);

        // 如果IP地址变更，检查是否与其他盒子冲突
        if (!box.getIpAddress().equals(request.getIpAddress())) {
            Optional<EdgeBox> existingBox = edgeBoxRepository.findByIpAddressAndDeletedAtIsNull(request.getIpAddress());
            if (existingBox.isPresent() && !existingBox.get().getId().equals(id)) {
                throw new BusinessException(ResultCode.BIZ_ERROR, "IP地址已被其他盒子使用");
            }
        }

        boxMapper.updateEntity(request, box);
        edgeBoxRepository.save(box);
        log.info("更新盒子成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteBox(Long id, Long currentSiteId) {
        EdgeBox box = getBoxAndCheckPermission(id, currentSiteId);

        // 逻辑删除盒子
        box.setDeletedAt(LocalDateTime.now());
        edgeBoxRepository.save(box);

        // 级联删除（逻辑删除）其下的所有通道
        channelService.getChannelsByBoxIdInternal(id).forEach(channel -> {
            channelService.deleteChannel(channel.getId(), currentSiteId);
        });

        log.info("删除盒子成功: id={}, name={}", id, box.getName());
    }

    @Override
    public void rebootBox(Long id, Long currentSiteId) {
        EdgeBox box = getBoxAndCheckPermission(id, currentSiteId);

        // 预留接口，目前返回开发中提示
        log.info("远程重启盒子请求: id={}, name={}", id, box.getName());
        throw new BusinessException(ResultCode.BIZ_ERROR, "远程重启功能开发中");
    }

    @Override
    public DeviceMetricsResponse getBoxMetrics(Long id, Long currentSiteId) {
        EdgeBox box = getBoxAndCheckPermission(id, currentSiteId);

        DeviceMetricsResponse response = new DeviceMetricsResponse();
        response.setBoxId(box.getId());
        response.setBoxName(box.getName());
        response.setCpuUsage(box.getCpuUsage());
        response.setCpuUsageText(formatPercentage(box.getCpuUsage()));
        response.setMemoryUsage(box.getMemoryUsage());
        response.setMemoryUsageText(formatPercentage(box.getMemoryUsage()));
        response.setDiskUsage(box.getDiskUsage());
        response.setDiskUsageText(formatPercentage(box.getDiskUsage()));
        response.setLastUpdateTime(box.getLastHeartbeatAt());

        // TODO: 历史趋势数据查询

        return response;
    }

    @Override
    public EdgeBox getBoxById(Long id) {
        return edgeBoxRepository.findByIdAndDeletedAtIsNull(id)
                .orElse(null);
    }

    @Override
    @Transactional
    public void updateHeartbeat(Long boxId, HeartbeatInfo heartbeat) {
        EdgeBox box = edgeBoxRepository.findByIdAndDeletedAtIsNull(boxId)
                .orElse(null);

        if (box == null) {
            log.warn("心跳对应的盒子不存在或已删除: boxId={}", boxId);
            return;
        }

        // 更新盒子状态
        box.setStatus(1); // 在线
        box.setLastHeartbeatAt(heartbeat.getTimestamp());

        // 更新资源使用率
        if (heartbeat.getCpuUsage() != null) {
            box.setCpuUsage(heartbeat.getCpuUsage());
        }
        if (heartbeat.getMemoryUsage() != null) {
            box.setMemoryUsage(heartbeat.getMemoryUsage());
        }
        if (heartbeat.getDiskUsage() != null) {
            box.setDiskUsage(heartbeat.getDiskUsage());
        }
        if (heartbeat.getVersion() != null) {
            box.setVersion(heartbeat.getVersion());
        }

        edgeBoxRepository.save(box);
        log.debug("心跳处理完成: boxId={}", boxId);
    }

    @Override
    @Transactional
    public void checkAndUpdateOfflineStatus() {
        if (!deviceConfig.getEnableAutoOffline()) {
            return;
        }

        int timeoutSeconds = deviceConfig.getHeartbeatTimeout();
        LocalDateTime timeoutTime = LocalDateTime.now().minusSeconds(timeoutSeconds);

        List<EdgeBox> timeoutBoxes = edgeBoxRepository.findTimeoutBoxes(timeoutTime);

        for (EdgeBox box : timeoutBoxes) {
            box.setStatus(0); // 离线
            edgeBoxRepository.save(box);
            log.info("盒子心跳超时，设置为离线: boxId={}, name={}, lastHeartbeat={}",
                    box.getId(), box.getName(), box.getLastHeartbeatAt());
        }

        if (!timeoutBoxes.isEmpty()) {
            log.info("心跳超时检测完成，共标记{}个盒子为离线", timeoutBoxes.size());
        }
    }

    @Override
    public BoxStatus getBoxStatus(Long boxId) {
        EdgeBox box = getBoxById(boxId);
        if (box == null) {
            return null;
        }

        return BoxStatus.builder()
                .boxId(boxId)
                .online(box.isOnline())
                .lastHeartbeatAt(box.getLastHeartbeatAt())
                .timeoutSeconds(deviceConfig.getHeartbeatTimeout())
                .build();
    }

    /**
     * 获取盒子并检查权限
     */
    private EdgeBox getBoxAndCheckPermission(Long id, Long currentSiteId) {
        EdgeBox box = edgeBoxRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ResultCode.BIZ_ERROR, "盒子不存在"));

        // 检查站点权限（非超级管理员需要检查）
        if (currentSiteId != null && !currentSiteId.equals(box.getSiteId())) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "无权访问该盒子");
        }

        return box;
    }

    /**
     * 格式化百分比
     */
    private String formatPercentage(Double value) {
        if (value == null) {
            return "-";
        }
        return String.format("%.1f%%", value);
    }
}
