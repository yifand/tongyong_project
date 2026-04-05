package com.vdc.pdi.systemconfig.controller;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.systemconfig.dto.request.AlgorithmConfigRequest;
import com.vdc.pdi.systemconfig.dto.request.BusinessRuleRequest;
import com.vdc.pdi.systemconfig.dto.request.GeneralConfigRequest;
import com.vdc.pdi.systemconfig.dto.response.AlgorithmConfigResponse;
import com.vdc.pdi.systemconfig.dto.response.BusinessRuleResponse;
import com.vdc.pdi.systemconfig.dto.response.ConfigGroupResponse;
import com.vdc.pdi.systemconfig.dto.response.ConfigResponse;
import com.vdc.pdi.systemconfig.service.AlgorithmConfigService;
import com.vdc.pdi.systemconfig.service.BusinessRuleService;
import com.vdc.pdi.systemconfig.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置管理Controller
 * 提供算法配置、业务规则、通用配置的管理接口
 */
@RestController
@RequestMapping("/api/v1/config")
@Tag(name = "系统配置管理", description = "算法配置、业务规则、通用配置管理")
@Validated
public class ConfigController {

    private final SystemConfigService systemConfigService;
    private final AlgorithmConfigService algorithmConfigService;
    private final BusinessRuleService businessRuleService;

    @Autowired
    public ConfigController(SystemConfigService systemConfigService,
                           AlgorithmConfigService algorithmConfigService,
                           BusinessRuleService businessRuleService) {
        this.systemConfigService = systemConfigService;
        this.algorithmConfigService = algorithmConfigService;
        this.businessRuleService = businessRuleService;
    }

    // ========== 算法配置接口 ==========

    /**
     * 获取通道算法配置
     */
    @GetMapping("/algorithm/{channelId}")
    @Operation(summary = "获取通道算法配置", description = "优先返回通道配置，不存在则返回全局配置")
    public ApiResponse<AlgorithmConfigResponse> getChannelAlgorithmConfig(
            @Parameter(description = "通道ID") @PathVariable("channelId") Long channelId,
            @Parameter(description = "算法类型(SMOKE/PDI_LEFT_FRONT/PDI_LEFT_REAR/PDI_SLICE)")
            @RequestParam("algorithmType") String algorithmType) {
        AlgorithmConfigResponse response = algorithmConfigService.getConfig(channelId, algorithmType);
        return ApiResponse.success(response);
    }

    /**
     * 更新通道算法配置
     */
    @PutMapping("/algorithm/{channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新通道算法配置")
    public ApiResponse<Void> updateChannelAlgorithmConfig(
            @Parameter(description = "通道ID") @PathVariable("channelId") Long channelId,
            @Valid @RequestBody AlgorithmConfigRequest request) {
        algorithmConfigService.updateConfig(channelId, request);
        return ApiResponse.success();
    }

    /**
     * 获取全局算法配置
     */
    @GetMapping("/algorithm/global")
    @Operation(summary = "获取全局算法配置")
    public ApiResponse<AlgorithmConfigResponse> getGlobalAlgorithmConfig(
            @Parameter(description = "算法类型")
            @RequestParam("algorithmType") String algorithmType) {
        AlgorithmConfigResponse response = algorithmConfigService.getGlobalConfig(algorithmType);
        return ApiResponse.success(response);
    }

    /**
     * 更新全局算法配置
     */
    @PutMapping("/algorithm/global")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "更新全局算法配置")
    public ApiResponse<Void> updateGlobalAlgorithmConfig(
            @Valid @RequestBody AlgorithmConfigRequest request) {
        algorithmConfigService.updateGlobalConfig(request);
        return ApiResponse.success();
    }

    // ========== 业务规则接口 ==========

    /**
     * 分页查询业务规则
     */
    @GetMapping("/rules")
    @Operation(summary = "分页查询业务规则")
    public ApiResponse<PageResult<BusinessRuleResponse>> listRules(
            @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @Parameter(description = "每页大小") @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "规则类型(STATE_TRANSITION/PDI_STANDARD_TIME/ALARM_THRESHOLD)")
            @RequestParam(name = "ruleType", required = false) String ruleType) {
        PageResult<BusinessRuleResponse> result = businessRuleService.listRules(page, size, ruleType);
        return ApiResponse.success(result);
    }

    /**
     * 获取业务规则详情
     */
    @GetMapping("/rules/{id}")
    @Operation(summary = "获取业务规则详情")
    public ApiResponse<BusinessRuleResponse> getRuleById(
            @Parameter(description = "规则ID") @PathVariable("id") Long id) {
        BusinessRuleResponse response = businessRuleService.getRule(id);
        return ApiResponse.success(response);
    }

    /**
     * 更新业务规则
     */
    @PutMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新业务规则")
    public ApiResponse<Void> updateRule(
            @Parameter(description = "规则ID") @PathVariable("id") Long id,
            @Valid @RequestBody BusinessRuleRequest request) {
        businessRuleService.updateRule(id, request);
        return ApiResponse.success();
    }

    /**
     * 启用业务规则
     */
    @PostMapping("/rules/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "启用业务规则")
    public ApiResponse<Void> enableRule(
            @Parameter(description = "规则ID") @PathVariable("id") Long id) {
        businessRuleService.enableRule(id, true);
        return ApiResponse.success();
    }

    /**
     * 禁用业务规则
     */
    @PostMapping("/rules/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "禁用业务规则")
    public ApiResponse<Void> disableRule(
            @Parameter(description = "规则ID") @PathVariable("id") Long id) {
        businessRuleService.enableRule(id, false);
        return ApiResponse.success();
    }

    // ========== 通用配置接口 ==========

    /**
     * 获取通用配置分组
     */
    @GetMapping("/general")
    @Operation(summary = "获取通用配置分组")
    public ApiResponse<ConfigGroupResponse> getGeneralConfig(
            @Parameter(description = "配置分组")
            @RequestParam("configGroup") String configGroup) {
        ConfigGroupResponse response = systemConfigService.getGeneralConfig(configGroup);
        return ApiResponse.success(response);
    }

    /**
     * 获取所有配置分组列表
     */
    @GetMapping("/general/groups")
    @Operation(summary = "获取所有配置分组列表")
    public ApiResponse<List<String>> listConfigGroups() {
        List<String> groups = systemConfigService.listConfigGroups();
        return ApiResponse.success(groups);
    }

    /**
     * 根据配置键获取配置
     */
    @GetMapping("/general/{configKey}")
    @Operation(summary = "根据配置键获取配置")
    public ApiResponse<ConfigResponse> getConfigByKey(
            @Parameter(description = "配置键") @PathVariable("configKey") String configKey) {
        ConfigResponse response = systemConfigService.getConfigByKey(configKey);
        return ApiResponse.success(response);
    }

    /**
     * 批量更新通用配置
     */
    @PutMapping("/general")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量更新通用配置")
    public ApiResponse<Void> updateGeneralConfig(
            @Valid @RequestBody GeneralConfigRequest request) {
        systemConfigService.updateGeneralConfig(request);
        return ApiResponse.success();
    }
}
