package com.vdc.pdi.ruleengine.domain.event;

import com.vdc.pdi.common.enums.AlarmTypeEnum;
import com.vdc.pdi.ruleengine.domain.vo.TaskDurationResult;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 报警触发事件
 * 当检测到PDI违规时触发
 */
@Getter
public class AlarmTriggerEvent extends ApplicationEvent {

    /**
     * 通道ID
     */
    private final Long channelId;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 报警类型
     */
    private final AlarmTypeEnum alarmType;

    /**
     * 触发时间
     */
    private final LocalDateTime triggerTime;

    /**
     * 作业时长结果
     */
    private final TaskDurationResult durationResult;

    /**
     * 报警描述
     */
    private final String description;

    /**
     * 状态流ID
     */
    private final Long stateStreamId;

    public AlarmTriggerEvent(Object source, Long channelId, Long siteId,
                             AlarmTypeEnum alarmType, LocalDateTime triggerTime,
                             TaskDurationResult durationResult, String description,
                             Long stateStreamId) {
        super(source);
        this.channelId = channelId;
        this.siteId = siteId;
        this.alarmType = alarmType;
        this.triggerTime = triggerTime;
        this.durationResult = durationResult;
        this.description = description;
        this.stateStreamId = stateStreamId;
    }

    /**
     * 创建PDI违规报警事件
     */
    public static AlarmTriggerEvent pdiViolation(Object source, Long channelId, Long siteId,
                                                  LocalDateTime triggerTime,
                                                  TaskDurationResult durationResult,
                                                  String description,
                                                  Long stateStreamId) {
        return new AlarmTriggerEvent(source, channelId, siteId,
                AlarmTypeEnum.PDI_VIOLATION, triggerTime, durationResult, description, stateStreamId);
    }
}
