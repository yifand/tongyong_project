package com.vdc.pdi.algorithminlet.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 状态流领域事件
 * 状态流数据处理后发布，供规则引擎监听处理
 */
@Data
@AllArgsConstructor
public class StateStreamEvent {

    /**
     * 状态流记录ID
     */
    private final Long streamId;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 盒子ID
     */
    private final String boxId;

    /**
     * 通道ID
     */
    private final String channelId;

    /**
     * 事件时间
     */
    private final LocalDateTime eventTime;

    /**
     * 状态码
     */
    private final Integer stateCode;

    /**
     * 状态三元组
     */
    private final StateTriple state;

    /**
     * 状态三元组
     */
    @Data
    @AllArgsConstructor
    public static class StateTriple {
        private final Integer doorOpen;
        private final Integer personPresent;
        private final Integer enteringExiting;
    }
}
