package com.vdc.pdi.ruleengine.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 规则触发事件
 * 状态机内部使用的事件
 */
@Getter
public class RuleTriggerEvent extends ApplicationEvent {

    /**
     * 触发类型枚举
     */
    public enum TriggerType {
        /**
         * 检测到进入
         */
        ENTRY_DETECTED,

        /**
         * 检测到离开
         */
        EXIT_DETECTED
    }

    /**
     * 通道ID
     */
    private final Long channelId;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 触发类型
     */
    private final TriggerType type;

    /**
     * 触发时间
     */
    private final LocalDateTime eventTime;

    /**
     * 进入时间（离开事件时有效）
     */
    private final LocalDateTime entryTime;

    /**
     * 当前任务ID
     */
    private final Long taskId;

    public RuleTriggerEvent(Object source, Long channelId, Long siteId,
                            TriggerType type, LocalDateTime eventTime,
                            LocalDateTime entryTime, Long taskId) {
        super(source);
        this.channelId = channelId;
        this.siteId = siteId;
        this.type = type;
        this.eventTime = eventTime;
        this.entryTime = entryTime;
        this.taskId = taskId;
    }

    /**
     * 创建进入检测事件
     */
    public static RuleTriggerEvent entryDetected(Object source, Long channelId, Long siteId,
                                                  LocalDateTime eventTime, Long taskId) {
        return new RuleTriggerEvent(source, channelId, siteId,
                TriggerType.ENTRY_DETECTED, eventTime, null, taskId);
    }

    /**
     * 创建离开检测事件
     */
    public static RuleTriggerEvent exitDetected(Object source, Long channelId, Long siteId,
                                                 LocalDateTime eventTime, LocalDateTime entryTime, Long taskId) {
        return new RuleTriggerEvent(source, channelId, siteId,
                TriggerType.EXIT_DETECTED, eventTime, entryTime, taskId);
    }
}
