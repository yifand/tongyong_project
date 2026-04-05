package com.vdc.pdi.ruleengine.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 作业结束事件
 * 当检测到人员离开车内时触发
 */
@Getter
public class TaskEndEvent extends ApplicationEvent {

    /**
     * 通道ID
     */
    private final Long channelId;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 结束时间
     */
    private final LocalDateTime endTime;

    /**
     * 任务ID
     */
    private final Long taskId;

    /**
     * 进入时间
     */
    private final LocalDateTime entryTime;

    /**
     * 状态流ID
     */
    private final Long stateStreamId;

    public TaskEndEvent(Object source, Long channelId, Long siteId,
                        LocalDateTime endTime, Long taskId,
                        LocalDateTime entryTime, Long stateStreamId) {
        super(source);
        this.channelId = channelId;
        this.siteId = siteId;
        this.endTime = endTime;
        this.taskId = taskId;
        this.entryTime = entryTime;
        this.stateStreamId = stateStreamId;
    }

    /**
     * 创建作业结束事件
     */
    public static TaskEndEvent of(Object source, Long channelId, Long siteId,
                                   LocalDateTime endTime, Long taskId,
                                   LocalDateTime entryTime, Long stateStreamId) {
        return new TaskEndEvent(source, channelId, siteId, endTime, taskId, entryTime, stateStreamId);
    }
}
