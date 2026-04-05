package com.vdc.pdi.ruleengine.controller;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.ruleengine.domain.vo.StateMachineInfoResponse;
import com.vdc.pdi.ruleengine.domain.vo.StateInfoResponse;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import com.vdc.pdi.ruleengine.service.StateMachineManager;
import com.vdc.pdi.ruleengine.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则引擎管理接口
 * 供管理员调试用
 */
@RestController
@RequestMapping("/api/v1/rule-engine")
@RequiredArgsConstructor
@Slf4j
public class RuleEngineController {

    private final RuleEngineService ruleEngineService;
    private final StateMachineManager stateMachineManager;

    /**
     * 获取通道当前状态
     */
    @GetMapping("/channels/{channelId}/state")
    public ApiResponse<StateInfoResponse> getChannelState(
            @PathVariable Long channelId,
            @RequestParam(required = false, defaultValue = "1") Long siteId) {
        log.debug("获取通道状态: channelId={}, siteId={}", channelId, siteId);

        StateCodeEnum state = ruleEngineService.getCurrentState(channelId, siteId);
        List<StateSnapshot> history = ruleEngineService.getStateHistory(channelId, siteId, 10);

        StateInfoResponse response = StateInfoResponse.builder()
                .channelId(channelId)
                .siteId(siteId)
                .currentState(state)
                .stateName(state != null ? state.getDescription() : null)
                .recentHistory(history)
                .queryTime(LocalDateTime.now())
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 重置通道状态机
     */
    @PostMapping("/channels/{channelId}/reset")
    public ApiResponse<Void> resetChannelState(
            @PathVariable Long channelId,
            @RequestParam(required = false, defaultValue = "1") Long siteId) {
        log.info("重置通道状态机: channelId={}, siteId={}", channelId, siteId);
        ruleEngineService.resetStateMachine(channelId, siteId);
        return ApiResponse.success();
    }

    /**
     * 手动触发状态处理（用于测试或补偿）
     */
    @PostMapping("/channels/{channelId}/process")
    public ApiResponse<Void> manualProcessState(
            @PathVariable Long channelId,
            @RequestParam(required = false, defaultValue = "1") Long siteId,
            @RequestParam Integer stateCode) {
        log.info("手动触发状态处理: channelId={}, siteId={}, stateCode={}", channelId, siteId, stateCode);
        ruleEngineService.manualProcessState(channelId, siteId, stateCode);
        return ApiResponse.success();
    }

    /**
     * 获取活跃状态机列表
     */
    @GetMapping("/state-machines")
    public ApiResponse<List<StateMachineInfoResponse>> getActiveStateMachines() {
        log.debug("获取活跃状态机列表");

        List<String> keys = stateMachineManager.getActiveStateMachineKeys();
        List<StateMachineInfoResponse> result = keys.stream()
                .map(key -> {
                    String[] parts = key.split(":");
                    Long siteId = Long.parseLong(parts[0]);
                    Long channelId = Long.parseLong(parts[1]);
                    StateCodeEnum state = stateMachineManager.getCurrentState(channelId, siteId);

                    return StateMachineInfoResponse.builder()
                            .key(key)
                            .siteId(siteId)
                            .channelId(channelId)
                            .currentState(state)
                            .stateName(state != null ? state.getDescription() : null)
                            .build();
                })
                .toList();

        return ApiResponse.success(result);
    }

    /**
     * 获取活跃状态机统计
     */
    @GetMapping("/state-machines/statistics")
    public ApiResponse<StateMachineStatisticsResponse> getStateMachineStatistics() {
        log.debug("获取状态机统计");

        int activeCount = stateMachineManager.getActiveStateMachineCount();

        StateMachineStatisticsResponse response = StateMachineStatisticsResponse.builder()
                .activeCount(activeCount)
                .queryTime(LocalDateTime.now())
                .build();

        return ApiResponse.success(response);
    }

    // 响应DTO

    /**
     * 状态机统计响应
     */
    @lombok.Data
    @lombok.Builder
    public static class StateMachineStatisticsResponse {
        private int activeCount;
        private LocalDateTime queryTime;
    }
}
