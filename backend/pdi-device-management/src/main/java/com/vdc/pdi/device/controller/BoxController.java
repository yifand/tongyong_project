package com.vdc.pdi.device.controller;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.device.dto.request.BoxQueryRequest;
import com.vdc.pdi.device.dto.request.BoxRequest;
import com.vdc.pdi.device.dto.response.BoxResponse;
import com.vdc.pdi.device.dto.response.DeviceMetricsResponse;
import com.vdc.pdi.device.service.BoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 盒子管理控制器
 */
@RestController
@RequestMapping("/api/v1/devices/boxes")
@RequiredArgsConstructor
@Tag(name = "边缘盒子管理", description = "边缘计算盒子的CRUD及远程操作")
public class BoxController {

    private final BoxService boxService;

    /**
     * 查询盒子列表
     */
    @GetMapping
    @Operation(summary = "查询盒子列表", description = "支持分页、关键字搜索、状态过滤")
    public ApiResponse<PageResponse<BoxResponse>> listBoxes(@Valid BoxQueryRequest request) {
        // TODO: 从SecurityContext获取当前用户站点ID
        Long currentSiteId = null;
        PageResponse<BoxResponse> result = boxService.listBoxes(request, currentSiteId);
        return ApiResponse.success(result);
    }

    /**
     * 获取盒子详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取盒子详情")
    public ApiResponse<BoxResponse> getBox(
            @Parameter(description = "盒子ID") @PathVariable("id") Long id) {
        Long currentSiteId = null;
        BoxResponse response = boxService.getBox(id, currentSiteId);
        return ApiResponse.success(response);
    }

    /**
     * 创建盒子
     */
    @PostMapping
    @Operation(summary = "创建盒子")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> createBox(@Valid @RequestBody BoxRequest request) {
        Long currentSiteId = null;
        Long currentUserId = 1L; // TODO: 从SecurityContext获取
        Long boxId = boxService.createBox(request, currentSiteId, currentUserId);
        return ApiResponse.success(boxId);
    }

    /**
     * 更新盒子
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新盒子")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateBox(
            @Parameter(description = "盒子ID") @PathVariable("id") Long id,
            @Valid @RequestBody BoxRequest request) {
        Long currentSiteId = null;
        boxService.updateBox(id, request, currentSiteId);
        return ApiResponse.success();
    }

    /**
     * 删除盒子
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除盒子")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBox(
            @Parameter(description = "盒子ID") @PathVariable("id") Long id) {
        Long currentSiteId = null;
        boxService.deleteBox(id, currentSiteId);
        return ApiResponse.success();
    }

    /**
     * 远程重启盒子（预留接口）
     */
    @PostMapping("/{id}/reboot")
    @Operation(summary = "远程重启盒子", description = "当前版本预留接口")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rebootBox(
            @Parameter(description = "盒子ID") @PathVariable("id") Long id) {
        Long currentSiteId = null;
        boxService.rebootBox(id, currentSiteId);
        return ApiResponse.success();
    }

    /**
     * 获取盒子资源使用率
     */
    @GetMapping("/{id}/metrics")
    @Operation(summary = "获取盒子资源使用率")
    public ApiResponse<DeviceMetricsResponse> getBoxMetrics(
            @Parameter(description = "盒子ID") @PathVariable("id") Long id) {
        Long currentSiteId = null;
        DeviceMetricsResponse response = boxService.getBoxMetrics(id, currentSiteId);
        return ApiResponse.success(response);
    }
}
