package com.pdi.service.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 盒子设备DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class BoxDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    private Long id;

    /**
     * 盒子编号
     */
    @NotBlank(message = "盒子编号不能为空")
    private String boxCode;

    /**
     * 盒子名称
     */
    @NotBlank(message = "盒子名称不能为空")
    private String boxName;

    /**
     * 所属点位ID
     */
    @NotNull(message = "所属点位不能为空")
    private Long siteId;

    /**
     * 点位名称
     */
    private String siteName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 状态: 0-离线, 1-在线, 2-故障
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 描述
     */
    private String description;

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
     * 通道数量
     */
    private Integer channelCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
