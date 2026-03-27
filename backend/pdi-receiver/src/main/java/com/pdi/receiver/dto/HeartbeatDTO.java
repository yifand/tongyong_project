package com.pdi.receiver.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 心跳DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class HeartbeatDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * CPU使用率(%)
     */
    private Double cpuUsage;

    /**
     * 内存使用率(%)
     */
    private Double memoryUsage;

    /**
     * 磁盘使用率(%)
     */
    private Double diskUsage;

    /**
     * GPU使用率(%)
     */
    private Double gpuUsage;

    /**
     * 软件版本
     */
    private String softwareVersion;

    /**
     * 算法版本
     */
    private String algorithmVersion;

    /**
     * 运行时长(秒)
     */
    private Long uptime;

}
