package com.vdc.pdi.device.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 心跳信息值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatInfo {

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * CPU使用率
     */
    private Double cpuUsage;

    /**
     * 内存使用率
     */
    private Double memoryUsage;

    /**
     * 磁盘使用率
     */
    private Double diskUsage;

    /**
     * 版本号
     */
    private String version;
}
