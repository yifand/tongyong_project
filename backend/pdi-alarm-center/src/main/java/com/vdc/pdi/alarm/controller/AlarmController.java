package com.vdc.pdi.alarm.controller;

import com.vdc.pdi.alarm.dto.request.AlarmHistoryRequest;
import com.vdc.pdi.alarm.dto.request.AlarmProcessRequest;
import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.dto.response.AlarmStatisticsResponse;
import com.vdc.pdi.alarm.service.AlarmSSEService;
import com.vdc.pdi.alarm.service.AlarmService;
import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.dto.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 报警控制器
 */
@RestController
@RequestMapping("/api/v1/alarms")
@Tag(name = "预警中心", description = "报警记录管理相关接口")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final AlarmSSEService alarmSSEService;

    /**
     * 获取实时预警列表（最近N条）
     */
    @GetMapping("/realtime")
    @Operation(summary = "实时预警列表", description = "获取最近发生的报警记录，默认最近50条")
    public ApiResponse<List<AlarmResponse>> getRealtimeAlarms(
            @RequestParam(defaultValue = "50") @Max(100) Integer limit,
            @RequestParam(required = false) Integer type) {
        return ApiResponse.success(alarmService.getRealtimeAlarms(limit, type));
    }

    /**
     * SSE订阅端点 - 实时推送新报警
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE实时推送", description = "建立SSE连接，实时接收新报警事件")
    public SseEmitter subscribeAlarms() {
        return alarmSSEService.subscribe();
    }

    /**
     * 历史预警分页查询
     */
    @GetMapping("/history")
    @Operation(summary = "历史预警查询", description = "支持多维度筛选的历史报警分页查询")
    public ApiResponse<PageResult<AlarmResponse>> getHistoryAlarms(
            @Valid @ModelAttribute AlarmHistoryRequest request) {
        return ApiResponse.success(alarmService.getHistoryAlarms(request));
    }

    /**
     * 预警详情查询
     */
    @GetMapping("/{id}")
    @Operation(summary = "预警详情", description = "根据ID获取报警详细信息")
    public ApiResponse<AlarmResponse> getAlarmDetail(@PathVariable Long id) {
        return ApiResponse.success(alarmService.getAlarmDetail(id));
    }

    /**
     * 标记已处理
     */
    @PatchMapping("/{id}/process")
    @Operation(summary = "标记已处理", description = "将报警状态更新为已处理")
    public ApiResponse<Void> processAlarm(
            @PathVariable Long id,
            @RequestBody(required = false) AlarmProcessRequest request) {
        alarmService.processAlarm(id, request);
        return ApiResponse.success();
    }

    /**
     * 标记误报
     */
    @PatchMapping("/{id}/false-positive")
    @Operation(summary = "标记误报", description = "将报警标记为误报")
    public ApiResponse<Void> markFalsePositive(
            @PathVariable Long id,
            @RequestBody(required = false) AlarmProcessRequest request) {
        alarmService.markFalsePositive(id, request);
        return ApiResponse.success();
    }

    /**
     * 今日预警统计
     */
    @GetMapping("/statistics/today")
    @Operation(summary = "今日统计", description = "获取今日报警统计数据")
    public ApiResponse<AlarmStatisticsResponse> getTodayStatistics() {
        return ApiResponse.success(alarmService.getTodayStatistics());
    }
}
