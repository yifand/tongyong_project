package com.vdc.pdi.device.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 盒子响应
 */
@Data
public class BoxResponse {

    /**
     * 盒子ID
     */
    private Long id;

    /**
     * 盒子名称
     */
    private String name;

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
     * 状态：0-离线，1-在线
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeatAt;

    /**
     * 软件版本号
     */
    private String version;

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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
