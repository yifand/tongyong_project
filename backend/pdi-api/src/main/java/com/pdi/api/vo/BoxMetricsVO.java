package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盒子资源指标VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 盒子名称
     */
    private String boxName;

    /**
     * CPU使用率
     */
    private BigDecimal cpuUsage;

    /**
     * 内存使用率
     */
    private BigDecimal memoryUsage;

    /**
     * 内存总量（MB）
     */
    private Long memoryTotal;

    /**
     * 内存使用（MB）
     */
    private Long memoryUsed;

    /**
     * 磁盘使用率
     */
    private BigDecimal diskUsage;

    /**
     * 磁盘总量（MB）
     */
    private Long diskTotal;

    /**
     * 磁盘使用（MB）
     */
    private Long diskUsed;

    /**
     * GPU使用率
     */
    private BigDecimal gpuUsage;

    /**
     * GPU内存使用率
     */
    private BigDecimal gpuMemoryUsage;

    /**
     * 网络入流量（字节）
     */
    private Long networkIn;

    /**
     * 网络出流量（字节）
     */
    private Long networkOut;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}
