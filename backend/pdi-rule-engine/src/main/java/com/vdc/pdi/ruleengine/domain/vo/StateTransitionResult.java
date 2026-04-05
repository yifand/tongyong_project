package com.vdc.pdi.ruleengine.domain.vo;

import com.vdc.pdi.common.enums.StateCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 状态转换结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateTransitionResult {

    /**
     * 是否有效转换
     */
    private boolean valid;

    /**
     * 原状态
     */
    private StateCodeEnum fromState;

    /**
     * 新状态
     */
    private StateCodeEnum toState;

    /**
     * 状态发生时间
     */
    private LocalDateTime timestamp;

    /**
     * 触发的规则事件类型
     */
    private RuleTriggerType triggerType;

    /**
     * 进入时间（离开事件时有效）
     */
    private LocalDateTime entryTime;

    /**
     * 关联原始状态流记录ID
     */
    private Long stateStreamId;

    /**
     * 错误信息（转换无效时）
     */
    private String errorMessage;

    /**
     * 规则触发事件类型
     */
    public enum RuleTriggerType {
        /**
         * 检测到进入
         */
        ENTRY_DETECTED,

        /**
         * 检测到离开
         */
        EXIT_DETECTED,

        /**
         * 无触发
         */
        NONE
    }

    /**
     * 创建无效转换结果
     */
    public static StateTransitionResult invalid(StateCodeEnum fromState, StateCodeEnum toState, String errorMessage) {
        return StateTransitionResult.builder()
                .valid(false)
                .fromState(fromState)
                .toState(toState)
                .errorMessage(errorMessage)
                .triggerType(RuleTriggerType.NONE)
                .build();
    }

    /**
     * 创建成功转换结果
     */
    public static StateTransitionResult success(StateCodeEnum fromState, StateCodeEnum toState,
                                                 LocalDateTime timestamp, Long stateStreamId) {
        return StateTransitionResult.builder()
                .valid(true)
                .fromState(fromState)
                .toState(toState)
                .timestamp(timestamp)
                .stateStreamId(stateStreamId)
                .triggerType(RuleTriggerType.NONE)
                .build();
    }

    /**
     * 创建带触发事件的转换结果
     */
    public static StateTransitionResult withTrigger(StateCodeEnum fromState, StateCodeEnum toState,
                                                     LocalDateTime timestamp, Long stateStreamId,
                                                     RuleTriggerType triggerType, LocalDateTime entryTime) {
        return StateTransitionResult.builder()
                .valid(true)
                .fromState(fromState)
                .toState(toState)
                .timestamp(timestamp)
                .stateStreamId(stateStreamId)
                .triggerType(triggerType)
                .entryTime(entryTime)
                .build();
    }
}
