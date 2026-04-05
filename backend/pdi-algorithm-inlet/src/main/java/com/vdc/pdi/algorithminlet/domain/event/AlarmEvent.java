package com.vdc.pdi.algorithminlet.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警领域事件
 * 报警事件处理后发布，供规则引擎监听处理
 */
@Data
@AllArgsConstructor
public class AlarmEvent {

    /**
     * 报警记录ID
     */
    private final Long alarmId;

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
     * 报警类型
     */
    private final String alarmType;

    /**
     * 报警时间
     */
    private final LocalDateTime alarmTime;

    /**
     * 图片URL
     */
    private final String imageUrl;

    /**
     * 置信度
     */
    private final Double confidence;

    /**
     * 位置
     */
    private final String location;
}
