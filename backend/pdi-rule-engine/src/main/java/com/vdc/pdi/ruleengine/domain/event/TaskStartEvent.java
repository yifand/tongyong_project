package com.vdc.pdi.ruleengine.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 作业开始事件
 * 当检测到人员进入车内时触发
 */
@Getter
public class TaskStartEvent extends ApplicationEvent {

    /**
     * 通道ID
     */
    private final Long channelId;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 开始时间
     */
    private final LocalDateTime startTime;

    /**
     * 任务ID
     */
    private final Long taskId;

    /**
     * 状态流ID
     */
    private final Long stateStreamId;

    public TaskStartEvent(Object source, Long channelId, Long siteId,
                          LocalDateTime startTime, Long taskId, Long stateStreamId) {
        super(source);
        this.channelId = channelId;
        this.siteId = siteId;
        this.startTime = startTime;
        this.taskId = taskId;
        this.stateStreamId = stateStreamId;
    }

    /**
     * 创建作业开始事件
     */
    public static TaskStartEvent of(Object source, Long channelId, Long siteId,
                                     LocalDateTime startTime, Long taskId, Long stateStreamId) {
        return new TaskStartEvent(source, channelId, siteId, startTime, taskId, stateStreamId);
    }
}
