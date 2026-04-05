package com.vdc.pdi.algorithminlet.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心跳领域事件
 * 心跳数据处理后发布，供监控模块监听处理
 */
@Data
@AllArgsConstructor
public class HeartbeatEvent {

    /**
     * 盒子记录ID
     */
    private final Long boxRecordId;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 盒子编码
     */
    private final String boxCode;

    /**
     * 心跳时间
     */
    private final LocalDateTime heartbeatTime;

    /**
     * CPU使用率
     */
    private final Double cpuUsage;

    /**
     * 内存使用率
     */
    private final Double memoryUsage;

    /**
     * 磁盘使用率
     */
    private final Double diskUsage;
}
