package com.pdi.receiver.service;

import com.pdi.common.enums.PDITaskStatusEnum;
import com.pdi.common.enums.StateCodeEnum;
import com.pdi.dao.entity.PDITask;
import com.pdi.dao.entity.StateStream;
import com.pdi.dao.mapper.PDITaskMapper;
import com.pdi.dao.mapper.StateStreamMapper;
import com.pdi.receiver.dto.StateStreamDTO;
import com.pdi.receiver.statemachine.PDIEvent;
import com.pdi.receiver.statemachine.PDIState;
import com.pdi.receiver.statemachine.PDITaskManager;
import com.pdi.receiver.websocket.AlarmWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 状态流处理服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class StateStreamServiceImpl implements StateStreamService {

    @Autowired
    private StateStreamMapper stateStreamMapper;

    @Autowired
    private PDITaskMapper pdiTaskMapper;

    @Autowired
    private PDITaskManager taskManager;

    @Autowired
    private AlarmWebSocketHandler webSocketHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processStateStream(StateStreamDTO dto) {
        // 1. 保存状态流到数据库
        StateStream stateStream = convertToEntity(dto);
        if (stateStream.getEventTime() == null) {
            stateStream.setEventTime(LocalDateTime.now());
        }
        stateStreamMapper.insert(stateStream);

        // 2. 处理状态机转换
        processStateTransition(dto);

        // 3. WebSocket推送状态更新
        pushStateUpdate(dto);
    }

    /**
     * 处理状态转换
     *
     * @param dto 状态流DTO
     */
    private void processStateTransition(StateStreamDTO dto) {
        Long channelId = dto.getChannelId();
        String newStateCode = dto.getStateCode();
        String currentStateCode = taskManager.getChannelState(channelId);

        if (newStateCode == null) {
            newStateCode = calculateStateCode(dto.getDoorOpen(), dto.getPersonPresent(), dto.getPersonEnteringExiting());
        }

        if (newStateCode == null) {
            log.warn("无法确定状态码: channelId={}", channelId);
            return;
        }

        // 状态未变化
        if (newStateCode.equals(currentStateCode)) {
            return;
        }

        PDIState currentState = PDIState.getByCode(currentStateCode);
        PDIState newState = PDIState.getByCode(newStateCode);

        if (currentState == null || newState == null) {
            log.warn("无效的状态码: current={}, new={}", currentStateCode, newStateCode);
            return;
        }

        log.info("状态转换: channelId={}, {} -> {}", channelId, currentStateCode, newStateCode);

        // 处理状态转换逻辑
        handleStateChange(channelId, dto, currentState, newState);

        // 更新通道状态
        taskManager.updateChannelState(channelId, newStateCode);
    }

    /**
     * 处理状态变化
     */
    private void handleStateChange(Long channelId, StateStreamDTO dto, PDIState fromState, PDIState toState) {
        // S1 -> S3: 门打开
        if (fromState == PDIState.S1 && toState == PDIState.S3) {
            log.debug("门打开: channelId={}", channelId);
        }
        // S3 -> S7: 检测到有人
        else if (fromState == PDIState.S3 && toState == PDIState.S7) {
            log.debug("检测到有人: channelId={}", channelId);
        }
        // S7 -> S8: 检测到进入动作
        else if (fromState == PDIState.S7 && toState == PDIState.S8) {
            log.debug("检测到进入动作: channelId={}", channelId);
        }
        // S8 -> S5: 进入完成（门关）
        else if (fromState == PDIState.S8 && toState == PDIState.S5) {
            log.info("人员进入完成，开始PDI: channelId={}", channelId);
            Long taskId = taskManager.startTask(channelId, dto.getBoxId(), dto.getSiteId());
            
            // 更新PDI任务的进入图片
            if (dto.getImageUrl() != null && taskId != null) {
                PDITask task = new PDITask();
                task.setId(taskId);
                task.setStartImageUrl(dto.getImageUrl());
                pdiTaskMapper.updateById(task);
            }
        }
        // S5 -> S8: 门打开，准备离开
        else if (fromState == PDIState.S5 && toState == PDIState.S8) {
            log.info("门打开，准备离开: channelId={}", channelId);
        }
        // S8 -> S1: 离开完成（门关）
        else if (fromState == PDIState.S8 && toState == PDIState.S1) {
            Long taskId = taskManager.getCurrentTask(channelId);
            if (taskId != null) {
                log.info("人员离开完成，结束PDI: channelId={}, taskId={}", channelId, taskId);
                taskManager.completeTask(channelId, taskId);
                
                // 更新PDI任务的离开图片
                if (dto.getImageUrl() != null) {
                    PDITask task = new PDITask();
                    task.setId(taskId);
                    task.setEndImageUrl(dto.getImageUrl());
                    pdiTaskMapper.updateById(task);
                }
            }
        }
        // S3 -> S1: 门关闭（无人进入）
        else if (fromState == PDIState.S3 && toState == PDIState.S1) {
            log.debug("门关闭（无人进入）: channelId={}", channelId);
        }
        // S7 -> S3: 人离开（未进入）
        else if (fromState == PDIState.S7 && toState == PDIState.S3) {
            log.debug("人离开（未进入）: channelId={}", channelId);
        }
    }

    /**
     * 推送状态更新
     */
    private void pushStateUpdate(StateStreamDTO dto) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "state_update");
            update.put("channelId", dto.getChannelId());
            update.put("boxId", dto.getBoxId());
            update.put("siteId", dto.getSiteId());
            update.put("stateCode", dto.getStateCode());
            update.put("doorOpen", dto.getDoorOpen());
            update.put("personPresent", dto.getPersonPresent());
            update.put("personEnteringExiting", dto.getPersonEnteringExiting());
            update.put("timestamp", System.currentTimeMillis());

            webSocketHandler.sendStateUpdate(update);
        } catch (Exception e) {
            log.error("推送状态更新失败", e);
        }
    }

    @Override
    public String calculateStateCode(Integer doorOpen, Integer personPresent, Integer personEnteringExiting) {
        StateCodeEnum stateCode = StateCodeEnum.getByStates(doorOpen, personPresent, personEnteringExiting);
        return stateCode != null ? stateCode.getCode() : null;
    }

    /**
     * 转换为实体
     */
    private StateStream convertToEntity(StateStreamDTO dto) {
        StateStream entity = new StateStream();
        entity.setChannelId(dto.getChannelId());
        entity.setBoxId(dto.getBoxId());
        entity.setSiteId(dto.getSiteId());
        entity.setDoorOpen(dto.getDoorOpen());
        entity.setPersonPresent(dto.getPersonPresent());
        entity.setPersonEnteringExiting(dto.getPersonEnteringExiting());
        entity.setStateCode(dto.getStateCode());
        entity.setDoorConfidence(dto.getDoorConfidence());
        entity.setPersonConfidence(dto.getPersonConfidence());
        entity.setFrameId(dto.getFrameId());
        entity.setImageUrl(dto.getImageUrl());
        entity.setProcessed(1);
        return entity;
    }

}
