package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 边缘盒子VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    private Long id;

    /**
     * 盒子编码
     */
    private String boxCode;

    /**
     * 盒子名称
     */
    private String boxName;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 状态（0-离线，1-在线，2-故障）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * CPU使用率
     */
    private BigDecimal cpuUsage;

    /**
     * 内存使用率
     */
    private BigDecimal memoryUsage;

    /**
     * 磁盘使用率
     */
    private BigDecimal diskUsage;

    /**
     * GPU使用率
     */
    private BigDecimal gpuUsage;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 软件版本
     */
    private String softwareVersion;

    /**
     * 算法版本
     */
    private String algorithmVersion;

    /**
     * 通道数量
     */
    private Integer channelCount;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
