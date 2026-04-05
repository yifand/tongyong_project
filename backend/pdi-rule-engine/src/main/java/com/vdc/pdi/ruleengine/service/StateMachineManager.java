package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.ruleengine.domain.entity.StateMachine;
import com.vdc.pdi.ruleengine.domain.event.StateStreamEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskEndEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskStartEvent;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import com.vdc.pdi.ruleengine.domain.vo.StateTransitionResult;
import com.vdc.pdi.ruleengine.exception.RuleEngineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 状态机管理器
 * 负责按channelId管理状态机实例的生命周期
 */
@Service
@Slf4j
public class StateMachineManager {

    /**
     * channelId -> StateMachine 映射表
     */
    private final ConcurrentHashMap<String, StateMachine> stateMachineMap;

    /**
     * 状态机最后访问时间记录（用于清理）
     */
    private final ConcurrentHashMap<String, LocalDateTime> lastAccessTimeMap;

    /**
     * 任务ID生成器
     */
    private final AtomicLong taskIdGenerator;

    /**
     * 状态机空闲超时时间（分钟）
     */
    @Value("${rule-engine.state-machine.idle-timeout:30}")
    private int idleTimeoutMinutes;

    private final ApplicationEventPublisher eventPublisher;

    public StateMachineManager(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.stateMachineMap = new ConcurrentHashMap<>();
        this.lastAccessTimeMap = new ConcurrentHashMap<>();
        this.taskIdGenerator = new AtomicLong(1);
    }

    /**
     * 获取或创建状态机实例
     */
    public StateMachine getOrCreateStateMachine(Long channelId, Long siteId) {
        String key = generateKey(channelId, siteId);

        StateMachine stateMachine = stateMachineMap.computeIfAbsent(key, k -> {
            StateMachine sm = new StateMachine();
            sm.initialize(channelId, siteId);
            log.info("创建状态机实例: channelId={}, siteId={}", channelId, siteId);
            return sm;
        });

        lastAccessTimeMap.put(key, LocalDateTime.now());
        return stateMachine;
    }

    /**
     * 获取状态机实例（如果不存在返回null）
     */
    public StateMachine getStateMachine(Long channelId, Long siteId) {
        String key = generateKey(channelId, siteId);
        return stateMachineMap.get(key);
    }

    /**
     * 获取通道当前状态
     */
    public StateCodeEnum getCurrentState(Long channelId, Long siteId) {
        StateMachine sm = getStateMachine(channelId, siteId);
        return sm != null ? sm.getCurrentState() : StateCodeEnum.S1;
    }

    /**
     * 获取通道状态历史
     */
    public List<StateSnapshot> getStateHistory(Long channelId, Long siteId, int limit) {
        StateMachine sm = getStateMachine(channelId, siteId);
        if (sm == null) {
            return List.of();
        }
        return sm.getRecentStates(limit);
    }

    /**
     * 处理状态流事件
     */
    @Transactional
    public void processStateStream(StateStreamEvent event) {
        Long channelId = event.getChannelId();
        Long siteId = event.getSiteId();
        StateCodeEnum newState = event.getStateCode();

        log.debug("处理状态流事件: channelId={}, siteId={}, state={}",
                channelId, siteId, newState);

        StateMachine stateMachine = getOrCreateStateMachine(channelId, siteId);

        // 执行状态转换
        StateTransitionResult result = stateMachine.transition(
                newState,
                event.getEventTime(),
                event.getStateStreamId()
        );

        if (!result.isValid()) {
            log.warn("无效状态转换: channelId={}, from={}, to={}, error={}",
                    channelId, result.getFromState(), result.getToState(), result.getErrorMessage());
            return;
        }

        // 处理规则触发事件
        if (result.getTriggerType() != StateTransitionResult.RuleTriggerType.NONE) {
            handleRuleTrigger(stateMachine, result);
        }
    }

    /**
     * 处理规则触发
     */
    private void handleRuleTrigger(StateMachine stateMachine, StateTransitionResult result) {
        Long channelId = stateMachine.getChannelId();
        Long siteId = stateMachine.getSiteId();

        switch (result.getTriggerType()) {
            case ENTRY_DETECTED -> {
                // 生成任务ID
                Long taskId = taskIdGenerator.getAndIncrement();
                stateMachine.setCurrentTaskId(taskId);

                TaskStartEvent startEvent = TaskStartEvent.of(
                        this,
                        channelId,
                        siteId,
                        result.getTimestamp(),
                        taskId,
                        result.getStateStreamId()
                );
                eventPublisher.publishEvent(startEvent);
                log.info("发布作业开始事件: channelId={}, taskId={}", channelId, taskId);
            }
            case EXIT_DETECTED -> {
                Long taskId = stateMachine.getCurrentTaskId();
                if (taskId == null) {
                    log.warn("离开事件但没有关联的任务ID: channelId={}", channelId);
                    taskId = 0L; // 使用0表示未知任务
                }

                TaskEndEvent endEvent = TaskEndEvent.of(
                        this,
                        channelId,
                        siteId,
                        result.getTimestamp(),
                        taskId,
                        result.getEntryTime(),
                        result.getStateStreamId()
                );
                eventPublisher.publishEvent(endEvent);
                log.info("发布作业结束事件: channelId={}, taskId={}", channelId, taskId);

                // 清除当前任务
                stateMachine.setCurrentTaskId(null);
            }
        }
    }

    /**
     * 重置通道状态机
     */
    public void resetStateMachine(Long channelId, Long siteId) {
        String key = generateKey(channelId, siteId);
        StateMachine sm = stateMachineMap.get(key);
        if (sm != null) {
            sm.reset();
            lastAccessTimeMap.put(key, LocalDateTime.now());
            log.info("重置状态机: channelId={}, siteId={}", channelId, siteId);
        }
    }

    /**
     * 移除状态机
     */
    public void removeStateMachine(Long channelId, Long siteId) {
        String key = generateKey(channelId, siteId);
        StateMachine removed = stateMachineMap.remove(key);
        lastAccessTimeMap.remove(key);
        if (removed != null) {
            log.info("移除状态机: channelId={}, siteId={}", channelId, siteId);
        }
    }

    /**
     * 获取活跃状态机数量
     */
    public int getActiveStateMachineCount() {
        return stateMachineMap.size();
    }

    /**
     * 获取所有活跃状态机的键
     */
    public List<String> getActiveStateMachineKeys() {
        return List.copyOf(stateMachineMap.keySet());
    }

    /**
     * 清除所有状态机（主要用于测试）
     */
    public void clearAllStateMachines() {
        stateMachineMap.clear();
        lastAccessTimeMap.clear();
        log.info("清除所有状态机实例");
    }

    /**
     * 清理空闲状态机（定时任务）
     */
    @Scheduled(fixedDelay = 600000) // 每10分钟执行
    public void cleanupIdleStateMachines() {
        LocalDateTime now = LocalDateTime.now();
        int cleanedCount = 0;

        for (String key : lastAccessTimeMap.keySet()) {
            LocalDateTime lastAccess = lastAccessTimeMap.get(key);

            if (lastAccess == null ||
                    Duration.between(lastAccess, now).toMinutes() > idleTimeoutMinutes) {

                StateMachine sm = stateMachineMap.remove(key);
                if (sm != null) {
                    lastAccessTimeMap.remove(key);
                    log.info("回收空闲状态机: key={}, idleTime={}分钟",
                            key, lastAccess != null ? Duration.between(lastAccess, now).toMinutes() : "unknown");
                    cleanedCount++;
                }
            }
        }

        if (cleanedCount > 0) {
            log.info("清理空闲状态机完成: 清理数量={}", cleanedCount);
        }
    }

    private String generateKey(Long channelId, Long siteId) {
        return siteId + ":" + channelId;
    }
}
