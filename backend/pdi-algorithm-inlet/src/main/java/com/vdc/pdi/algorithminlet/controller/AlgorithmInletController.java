package com.vdc.pdi.algorithminlet.controller;

import com.vdc.pdi.algorithminlet.dto.request.AlarmEventRequest;
import com.vdc.pdi.algorithminlet.dto.request.HeartbeatRequest;
import com.vdc.pdi.algorithminlet.dto.request.StateStreamRequest;
import com.vdc.pdi.algorithminlet.dto.response.InletResponse;
import com.vdc.pdi.algorithminlet.service.AlgorithmInletService;
import com.vdc.pdi.algorithminlet.service.HeartbeatService;
import com.vdc.pdi.algorithminlet.validator.BoxAuthResult;
import com.vdc.pdi.algorithminlet.validator.BoxAuthenticator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 算法数据入口控制器
 * 接收边缘盒子推送的状态流、报警事件、心跳数据
 */
@RestController
@RequestMapping("/api/v1/inlet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "算法数据入口", description = "接收边缘盒子推送的数据")
public class AlgorithmInletController {

    private final AlgorithmInletService inletService;
    private final HeartbeatService heartbeatService;
    private final BoxAuthenticator boxAuthenticator;

    /**
     * 接收状态三元组流
     */
    @PostMapping("/state")
    @Operation(summary = "接收状态流数据", description = "接收边缘盒子推送的状态三元组流数据")
    public ResponseEntity<InletResponse> receiveStateStream(
            @RequestHeader(value = "X-Box-Token", required = false) String boxToken,
            @Valid @RequestBody StateStreamRequest request) {

        log.debug("接收到状态流数据: boxId={}, channelId={}, stateCode={}",
                request.getBoxId(), request.getChannelId(), request.getStateCode());

        // 盒子认证
        BoxAuthResult authResult = boxAuthenticator.authenticate(boxToken, request.getBoxId());
        if (!authResult.isValid()) {
            log.warn("状态流认证失败: boxId={}, error={}", request.getBoxId(), authResult.getErrorMessage());
            return ResponseEntity.status(401)
                    .body(InletResponse.fail(401, "BOX_AUTH_FAILED", "盒子认证失败: " + authResult.getErrorMessage()));
        }

        // 处理状态流
        inletService.processStateStream(request, authResult.getSiteId());

        return ResponseEntity.ok(InletResponse.success());
    }

    /**
     * 接收报警事件
     */
    @PostMapping("/alarm")
    @Operation(summary = "接收报警事件", description = "接收边缘盒子推送的报警事件")
    public ResponseEntity<InletResponse> receiveAlarmEvent(
            @RequestHeader(value = "X-Box-Token", required = false) String boxToken,
            @Valid @RequestBody AlarmEventRequest request) {

        log.info("接收到报警事件: boxId={}, channelId={}, alarmType={}",
                request.getBoxId(), request.getChannelId(), request.getAlarmType());

        // 盒子认证
        BoxAuthResult authResult = boxAuthenticator.authenticate(boxToken, request.getBoxId());
        if (!authResult.isValid()) {
            log.warn("报警事件认证失败: boxId={}, error={}", request.getBoxId(), authResult.getErrorMessage());
            return ResponseEntity.status(401)
                    .body(InletResponse.fail(401, "BOX_AUTH_FAILED", "盒子认证失败: " + authResult.getErrorMessage()));
        }

        // 处理报警事件
        inletService.processAlarmEvent(request, authResult.getSiteId());

        return ResponseEntity.ok(InletResponse.success(Map.of("alarmId", System.currentTimeMillis())));
    }

    /**
     * 接收心跳数据
     */
    @PostMapping("/heartbeat")
    @Operation(summary = "接收心跳数据", description = "接收边缘盒子心跳数据")
    public ResponseEntity<InletResponse> receiveHeartbeat(
            @RequestHeader(value = "X-Box-Token", required = false) String boxToken,
            @Valid @RequestBody HeartbeatRequest request) {

        log.debug("接收到心跳数据: boxId={}, cpu={}%, memory={}%, disk={}%",
                request.getBoxId(),
                request.getCpuUsage(),
                request.getMemoryUsage(),
                request.getDiskUsage());

        // 盒子认证
        BoxAuthResult authResult = boxAuthenticator.authenticate(boxToken, request.getBoxId());
        if (!authResult.isValid()) {
            log.warn("心跳认证失败: boxId={}, error={}", request.getBoxId(), authResult.getErrorMessage());
            return ResponseEntity.status(401)
                    .body(InletResponse.fail(401, "BOX_AUTH_FAILED", "盒子认证失败: " + authResult.getErrorMessage()));
        }

        // 处理心跳
        heartbeatService.processHeartbeat(request, authResult.getSiteId());

        return ResponseEntity.ok(InletResponse.success());
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "算法数据入口健康检查")
    public ResponseEntity<InletResponse> health() {
        return ResponseEntity.ok(InletResponse.success(Map.of("status", "UP")));
    }
}
