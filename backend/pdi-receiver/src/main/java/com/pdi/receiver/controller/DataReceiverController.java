package com.pdi.receiver.controller;

import com.pdi.common.result.Result;
import com.pdi.receiver.dto.AlarmEventDTO;
import com.pdi.receiver.dto.HeartbeatDTO;
import com.pdi.receiver.dto.StateStreamDTO;
import com.pdi.receiver.service.AlarmEventService;
import com.pdi.receiver.service.HeartbeatService;
import com.pdi.receiver.service.StateStreamService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据接收控制器
 * 接收来自边缘盒子的数据
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@RestController
@RequestMapping("/api/receiver")
public class DataReceiverController {

    @Autowired
    private StateStreamService stateStreamService;

    @Autowired
    private AlarmEventService alarmEventService;

    @Autowired
    private HeartbeatService heartbeatService;

    /**
     * 接收状态流数据
     *
     * @param dto 状态流DTO
     * @return 处理结果
     */
    @PostMapping("/state-stream")
    public Result<Void> receiveStateStream(@Valid @RequestBody StateStreamDTO dto) {
        try {
            stateStreamService.processStateStream(dto);
            return Result.success();
        } catch (Exception e) {
            log.error("处理状态流数据失败: {}", e.getMessage(), e);
            return Result.error("处理状态流数据失败: " + e.getMessage());
        }
    }

    /**
     * 接收报警事件
     *
     * @param dto 报警事件DTO
     * @return 处理结果
     */
    @PostMapping("/alarm")
    public Result<Void> receiveAlarm(@Valid @RequestBody AlarmEventDTO dto) {
        try {
            alarmEventService.processAlarmEvent(dto);
            return Result.success();
        } catch (Exception e) {
            log.error("处理报警事件失败: {}", e.getMessage(), e);
            return Result.error("处理报警事件失败: " + e.getMessage());
        }
    }

    /**
     * 接收心跳
     *
     * @param dto 心跳DTO
     * @return 处理结果
     */
    @PostMapping("/heartbeat")
    public Result<Void> receiveHeartbeat(@Valid @RequestBody HeartbeatDTO dto) {
        try {
            heartbeatService.processHeartbeat(dto);
            return Result.success();
        } catch (Exception e) {
            log.error("处理心跳失败: {}", e.getMessage(), e);
            return Result.error("处理心跳失败: " + e.getMessage());
        }
    }

}
