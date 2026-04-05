package com.vdc.pdi.device.controller;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.device.dto.response.DeviceOverviewResponse;
import com.vdc.pdi.device.dto.response.SiteDeviceStatusResponse;
import com.vdc.pdi.device.service.DeviceStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设备状态监控控制器
 */
@RestController
@RequestMapping("/api/v1/devices/status")
@RequiredArgsConstructor
@Tag(name = "设备状态监控", description = "设备状态大屏数据接口")
public class DeviceStatusController {

    private final DeviceStatusService deviceStatusService;

    /**
     * 获取设备状态概览
     */
    @GetMapping("/overview")
    @Operation(summary = "设备状态概览", description = "获取设备状态统计数据，用于大屏展示")
    public ApiResponse<DeviceOverviewResponse> getOverview(
            @Parameter(description = "站点ID，超级管理员可指定") @RequestParam(required = false) Long siteId) {
        // TODO: 从SecurityContext获取当前用户站点ID和是否超级管理员
        Long currentSiteId = null;
        boolean isSuperAdmin = true;
        DeviceOverviewResponse response = deviceStatusService.getOverview(siteId, currentSiteId, isSuperAdmin);
        return ApiResponse.success(response);
    }

    /**
     * 获取所有站点设备状态统计
     */
    @GetMapping("/sites")
    @Operation(summary = "各站点设备状态统计")
    public ApiResponse<List<SiteDeviceStatusResponse>> getAllSitesStatus() {
        // TODO: 从SecurityContext获取当前用户站点ID和是否超级管理员
        Long currentSiteId = null;
        boolean isSuperAdmin = true;
        List<SiteDeviceStatusResponse> responses = deviceStatusService.getAllSitesStatus(currentSiteId, isSuperAdmin);
        return ApiResponse.success(responses);
    }
}
