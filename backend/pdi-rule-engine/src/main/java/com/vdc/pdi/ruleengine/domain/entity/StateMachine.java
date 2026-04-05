package com.vdc.pdi.ruleengine.domain.entity;

import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.ruleengine.domain.vo.SequenceMatchResult;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import com.vdc.pdi.ruleengine.domain.vo.StateTransitionResult;
import com.vdc.pdi.ruleengine.service.StateSequenceMatcher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * 状态机实例
 * 每个channelId对应一个独立的StateMachine实例
 */
@Data
@Slf4j
public class StateMachine {

    /**
     * 历史记录最大保留数量
     */
    private static final int MAX_HISTORY_SIZE = 100;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 当前状态
     */
    private StateCodeEnum currentState;

    /**
     * 历史状态序列（带时间戳）
     */
    private final LinkedList<StateSnapshot> stateHistory;

    /**
     * 当前PDI作业记录ID（进行中时有效）
     */
    private Long currentTaskId;

    /**
     * 进入时间（人员进入车内时记录）
     */
    private LocalDateTime entryTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 状态序列匹配器
     */
    private StateSequenceMatcher sequenceMatcher;

    public StateMachine() {
        this.stateHistory = new LinkedList<>();
        this.currentState = StateCodeEnum.S1;
        this.lastUpdateTime = LocalDateTime.now();
        this.sequenceMatcher = new StateSequenceMatcher();
    }

    /**
     * 初始化状态机
     */
    public void initialize(Long channelId, Long siteId) {
        this.channelId = channelId;
        this.siteId = siteId;
        this.currentState = StateCodeEnum.S1;
        this.stateHistory.clear();
        this.currentTaskId = null;
        this.entryTime = null;
        this.lastUpdateTime = LocalDateTime.now();
        log.debug("状态机初始化: channelId={}, siteId={}", channelId, siteId);
    }

    /**
     * 状态转换
     *
     * @param newState      新状态
     * @param timestamp     状态发生时间
     * @param stateStreamId 原始状态流ID
     * @return 转换结果
     */
    public StateTransitionResult transition(StateCodeEnum newState,
                                            LocalDateTime timestamp,
                                            Long stateStreamId) {
        // 验证转换是否有效
        if (!isValidTransition(currentState, newState)) {
            String errorMsg = String.format("无效状态转换: %s -> %s", currentState, newState);
            log.warn("{} (channelId={})", errorMsg, channelId);
            return StateTransitionResult.invalid(currentState, newState, errorMsg);
        }

        // 记录历史状态
        StateSnapshot snapshot = StateSnapshot.builder()
                .state(newState)
                .timestamp(timestamp)
                .stateStreamId(stateStreamId)
                .channelId(channelId)
                .siteId(siteId)
                .build();
        addToHistory(snapshot);

        // 执行状态转换
        StateCodeEnum oldState = this.currentState;
        this.currentState = newState;
        this.lastUpdateTime = timestamp;

        log.debug("状态转换: {} -> {} (channelId={})", oldState, newState, channelId);

        // 检查是否触发规则
        StateTransitionResult.RuleTriggerType triggerType = checkRuleTrigger();
        LocalDateTime triggerEntryTime = null;

        if (triggerType == StateTransitionResult.RuleTriggerType.ENTRY_DETECTED) {
            this.entryTime = timestamp;
            triggerEntryTime = timestamp;
            log.info("检测到人员进入: channelId={}, entryTime={}", channelId, timestamp);
        } else if (triggerType == StateTransitionResult.RuleTriggerType.EXIT_DETECTED) {
            triggerEntryTime = this.entryTime;
            this.entryTime = null;
            log.info("检测到人员离开: channelId={}, exitTime={}", channelId, timestamp);
        }

        return StateTransitionResult.withTrigger(
                oldState, newState, timestamp, stateStreamId,
                triggerType, triggerEntryTime
        );
    }

    /**
     * 验证状态转换是否有效
     */
    private boolean isValidTransition(StateCodeEnum from, StateCodeEnum to) {
        if (from == to) {
            return true; // 相同状态允许重复
        }

        return switch (from) {
            case S1 -> to == StateCodeEnum.S5 || to == StateCodeEnum.S7;
            case S3 -> to == StateCodeEnum.S8;
            case S5 -> to == StateCodeEnum.S1 || to == StateCodeEnum.S7;
            case S7 -> to == StateCodeEnum.S5 || to == StateCodeEnum.S8 || to == StateCodeEnum.S1 || to == StateCodeEnum.S3;
            case S8 -> to == StateCodeEnum.S3 || to == StateCodeEnum.S7;
        };
    }

    /**
     * 检查是否触发规则
     */
    private StateTransitionResult.RuleTriggerType checkRuleTrigger() {
        if (matchesEntrySequence()) {
            return StateTransitionResult.RuleTriggerType.ENTRY_DETECTED;
        }
        if (matchesExitSequence()) {
            return StateTransitionResult.RuleTriggerType.EXIT_DETECTED;
        }
        return StateTransitionResult.RuleTriggerType.NONE;
    }

    /**
     * 添加状态到历史记录
     */
    private void addToHistory(StateSnapshot snapshot) {
        stateHistory.addLast(snapshot);
        if (stateHistory.size() > MAX_HISTORY_SIZE) {
            stateHistory.removeFirst();
        }
    }

    /**
     * 获取最近N个状态序列
     */
    public List<StateSnapshot> getRecentStates(int n) {
        int size = Math.min(n, stateHistory.size());
        if (size == 0) {
            return List.of();
        }
        return List.copyOf(stateHistory.subList(stateHistory.size() - size, stateHistory.size()));
    }

    /**
     * 检查是否匹配进入序列 (S7 → S8 → S3)
     */
    public boolean matchesEntrySequence() {
        return sequenceMatcher.isLastStatesEntrySequence(stateHistory);
    }

    /**
     * 检查是否匹配离开序列 (S3 → S8 → S7 → S5/S1)
     */
    public boolean matchesExitSequence() {
        return sequenceMatcher.isLastStatesExitSequence(stateHistory);
    }

    /**
     * 获取进入时间点 (S3状态时间)
     */
    public LocalDateTime getEntryTime() {
        SequenceMatchResult result = sequenceMatcher.matchEntrySequence(stateHistory);
        if (!result.isMatched()) {
            return null;
        }
        return result.getTimestamp();
    }

    /**
     * 获取离开时间点 (S5或S1状态时间)
     */
    public LocalDateTime getExitTime() {
        SequenceMatchResult result = sequenceMatcher.matchExitSequence(stateHistory);
        if (!result.isMatched()) {
            return null;
        }
        return result.getTimestamp();
    }

    /**
     * 重置状态机
     */
    public void reset() {
        this.currentState = StateCodeEnum.S1;
        this.stateHistory.clear();
        this.currentTaskId = null;
        this.entryTime = null;
        this.lastUpdateTime = LocalDateTime.now();
        log.info("状态机重置: channelId={}", channelId);
    }

    /**
     * 获取完整历史记录
     */
    public List<StateSnapshot> getStateHistory() {
        return List.copyOf(stateHistory);
    }

    /**
     * 是否空闲（未进行PDI作业）
     */
    public boolean isIdle() {
        return currentState == StateCodeEnum.S1 || currentState == StateCodeEnum.S5;
    }

    /**
     * 是否有人在内（PDI作业中）
     */
    public boolean isOccupied() {
        return currentState == StateCodeEnum.S3;
    }
}
