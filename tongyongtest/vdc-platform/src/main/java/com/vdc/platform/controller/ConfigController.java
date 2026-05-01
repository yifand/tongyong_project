package com.vdc.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdc.platform.common.ApiResult;
import com.vdc.platform.dto.GeneralConfigRequest;
import com.vdc.platform.dto.RuleConfigRequest;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.entity.SystemConfig;
import com.vdc.platform.ruleengine.config.RuleConfigCache;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IOperationLogService;
import com.vdc.platform.service.IRuleConfigService;
import com.vdc.platform.service.ISystemConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final IRuleConfigService ruleConfigService;
    private final ISystemConfigService systemConfigService;
    private final IOperationLogService operationLogService;
    private final RuleConfigCache ruleConfigCache;

    private static final List<String> THRESHOLD_KEYS = List.of(
            "THRESHOLD_DURATION_WARNING",
            "THRESHOLD_PERSON_ABSENT"
    );

    private static final List<String> GENERAL_KEYS = List.of(
            "SYSTEM_NAME",
            "LOGO_URL",
            "ALARM_AUTO_CLEAR",
            "REPORT_RETENTION_DAYS"
    );

    @GetMapping("/rules")
    @PreAuthorize("hasAuthority('config:read') or hasAuthority('admin')")
    public ApiResult<List<RuleConfig>> listRules() {
        return ApiResult.success(ruleConfigService.list());
    }

    @PutMapping("/rules")
    @PreAuthorize("hasAuthority('config:write') or hasAuthority('admin')")
    public ApiResult<Void> updateRules(@Valid @RequestBody List<RuleConfigRequest> requests,
                                       HttpServletRequest httpRequest) {
        for (RuleConfigRequest req : requests) {
            LambdaQueryWrapper<RuleConfig> ruleWrapper = new LambdaQueryWrapper<>();
            ruleWrapper.eq(RuleConfig::getRuleName, req.getRuleName());
            RuleConfig config = ruleConfigService.getOne(ruleWrapper);
            if (config == null) {
                config = new RuleConfig();
                config.setRuleName(req.getRuleName());
                config.setChannelType(req.getChannelType());
            }
            config.setRequireVehicle(req.getRequireVehicle());
            config.setEnterPattern(req.getEnterPattern());
            config.setExitPattern(req.getExitPattern());
            config.setStandardDuration(req.getStandardDuration());
            config.setCriticalThresholdPct(req.getCriticalThresholdPct());
            config.setPersonAbsentTimeout(req.getPersonAbsentTimeout());
            config.setIsEnabled(req.getIsEnabled());
            if (config.getId() == null) {
                ruleConfigService.save(config);
            } else {
                ruleConfigService.updateById(config);
            }
        }
        ruleConfigCache.refresh();
        recordLog(getCurrentUser(), "CONFIG_CHANGE", "Updated rule configs: " + requests.stream().map(RuleConfigRequest::getRuleName).collect(java.util.stream.Collectors.joining(", ")), 1, httpRequest);
        return ApiResult.success();
    }

    @GetMapping("/thresholds")
    @PreAuthorize("hasAuthority('config:read') or hasAuthority('admin')")
    public ApiResult<List<SystemConfig>> listThresholds() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SystemConfig::getConfigKey, THRESHOLD_KEYS);
        List<SystemConfig> list = systemConfigService.list(wrapper);
        return ApiResult.success(list);
    }

    @PutMapping("/thresholds")
    @PreAuthorize("hasAuthority('config:write') or hasAuthority('admin')")
    public ApiResult<Void> updateThresholds(@Valid @RequestBody List<GeneralConfigRequest> requests,
                                            HttpServletRequest httpRequest) {
        for (GeneralConfigRequest req : requests) {
            SystemConfig existing = systemConfigService.getByKey(req.getConfigKey());
            if (existing == null) {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(req.getConfigKey());
                config.setConfigValue(req.getConfigValue());
                config.setDescription(req.getDescription());
                config.setUpdatedAt(LocalDateTime.now());
                systemConfigService.save(config);
            } else {
                existing.setConfigValue(req.getConfigValue());
                existing.setDescription(req.getDescription());
                existing.setUpdatedAt(LocalDateTime.now());
                systemConfigService.updateById(existing);
            }
        }
        recordLog(getCurrentUser(), "CONFIG_CHANGE", "Updated thresholds: " + requests.stream().map(GeneralConfigRequest::getConfigKey).collect(java.util.stream.Collectors.joining(", ")), 1, httpRequest);
        return ApiResult.success();
    }

    @GetMapping("/general")
    @PreAuthorize("hasAuthority('config:read') or hasAuthority('admin')")
    public ApiResult<List<SystemConfig>> getGeneralConfig() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SystemConfig::getConfigKey, GENERAL_KEYS);
        List<SystemConfig> list = systemConfigService.list(wrapper);
        return ApiResult.success(list);
    }

    @PutMapping("/general")
    @PreAuthorize("hasAuthority('config:write') or hasAuthority('admin')")
    public ApiResult<Void> updateGeneralConfig(@Valid @RequestBody List<GeneralConfigRequest> requests,
                                               HttpServletRequest httpRequest) {
        for (GeneralConfigRequest req : requests) {
            SystemConfig existing = systemConfigService.getByKey(req.getConfigKey());
            if (existing == null) {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(req.getConfigKey());
                config.setConfigValue(req.getConfigValue());
                config.setDescription(req.getDescription());
                config.setUpdatedAt(LocalDateTime.now());
                systemConfigService.save(config);
            } else {
                existing.setConfigValue(req.getConfigValue());
                existing.setDescription(req.getDescription());
                existing.setUpdatedAt(LocalDateTime.now());
                systemConfigService.updateById(existing);
            }
        }
        recordLog(getCurrentUser(), "CONFIG_CHANGE", "Updated general config: " + requests.stream().map(GeneralConfigRequest::getConfigKey).collect(java.util.stream.Collectors.joining(", ")), 1, httpRequest);
        return ApiResult.success();
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
