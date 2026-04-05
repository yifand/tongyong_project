package com.vdc.pdi.device.controller;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.device.dto.request.ChannelQueryRequest;
import com.vdc.pdi.device.dto.request.ChannelRequest;
import com.vdc.pdi.device.dto.response.ChannelResponse;
import com.vdc.pdi.device.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通道管理控制器
 */
@RestController
@RequestMapping("/api/v1/devices/channels")
@RequiredArgsConstructor
@Tag(name = "通道管理", description = "视频通道的CRUD及算法配置")
public class ChannelController {

    private final ChannelService channelService;

    /**
     * 查询通道列表
     */
    @GetMapping
    @Operation(summary = "查询通道列表", description = "支持分页、盒子过滤、算法类型过滤")
    public ApiResponse<PageResponse<ChannelResponse>> listChannels(@Valid ChannelQueryRequest request) {
        // TODO: 从SecurityContext获取当前用户站点ID
        Long currentSiteId = null;
        PageResponse<ChannelResponse> result = channelService.listChannels(request, currentSiteId);
        return ApiResponse.success(result);
    }

    /**
     * 获取通道详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取通道详情")
    public ApiResponse<ChannelResponse> getChannel(
            @Parameter(description = "通道ID") @PathVariable("id") Long id) {
        Long currentSiteId = null;
        ChannelResponse response = channelService.getChannel(id, currentSiteId);
        return ApiResponse.success(response);
    }

    /**
     * 创建通道
     */
    @PostMapping
    @Operation(summary = "创建通道")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> createChannel(@Valid @RequestBody ChannelRequest request) {
        Long currentSiteId = null;
        Long currentUserId = 1L; // TODO: 从SecurityContext获取
        Long channelId = channelService.createChannel(request, currentSiteId, currentUserId);
        return ApiResponse.success(channelId);
    }

    /**
     * 更新通道
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新通道")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateChannel(
            @Parameter(description = "通道ID") @PathVariable("id") Long id,
            @Valid @RequestBody ChannelRequest request) {
        Long currentSiteId = null;
        channelService.updateChannel(id, request, currentSiteId);
        return ApiResponse.success();
    }

    /**
     * 删除通道
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通道")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteChannel(
            @Parameter(description = "通道ID") @PathVariable("id") Long id) {
        Long currentSiteId = null;
        channelService.deleteChannel(id, currentSiteId);
        return ApiResponse.success();
    }

    /**
     * 根据盒子ID查询通道列表
     */
    @GetMapping("/box/{boxId}")
    @Operation(summary = "获取盒子的通道列表")
    public ApiResponse<List<ChannelResponse>> getChannelsByBoxId(
            @Parameter(description = "盒子ID") @PathVariable("boxId") Long boxId) {
        Long currentSiteId = null;
        List<ChannelResponse> channels = channelService.getChannelsByBoxId(boxId, currentSiteId);
        return ApiResponse.success(channels);
    }
}
