package com.vdc.pdi.ruleengine.domain.event;

import com.vdc.pdi.common.enums.StateCodeEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 状态流事件
 * 由算法数据入口模块发布，规则引擎监听处理
 */
@Getter
public class StateStreamEvent extends ApplicationEvent {

    /**
     * 通道ID
     */
    private final Long channelId;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 状态编码
     */
    private final StateCodeEnum stateCode;

    /**
     * 状态发生时间
     */
    private final LocalDateTime eventTime;

    /**
     * 状态流记录ID
     */
    private final Long stateStreamId;

    /**
     * 门状态 (0=关, 1=开)
     */
    private final Integer doorOpen;

    /**
     * 人员状态 (0=无人, 1=有人)
     */
    private final Integer personPresent;

    /**
     * 进出状态 (0=未进出, 1=进出中)
     */
    private final Integer enteringExiting;

    public StateStreamEvent(Object source, Long channelId, Long siteId,
                            StateCodeEnum stateCode, LocalDateTime eventTime,
                            Long stateStreamId, Integer doorOpen,
                            Integer personPresent, Integer enteringExiting) {
        super(source);
        this.channelId = channelId;
        this.siteId = siteId;
        this.stateCode = stateCode;
        this.eventTime = eventTime;
        this.stateStreamId = stateStreamId;
        this.doorOpen = doorOpen;
        this.personPresent = personPresent;
        this.enteringExiting = enteringExiting;
    }

    /**
     * 从状态码构造事件
     */
    public static StateStreamEvent of(Object source, Long channelId, Long siteId,
                                       Integer stateCode, LocalDateTime eventTime,
                                       Long stateStreamId) {
        StateCodeEnum state = StateCodeEnum.fromCode(stateCode);
        return new StateStreamEvent(
                source, channelId, siteId, state, eventTime, stateStreamId,
                state.getDoorOpen(), state.getPersonPresent(), state.getEnteringExiting()
        );
    }
}
