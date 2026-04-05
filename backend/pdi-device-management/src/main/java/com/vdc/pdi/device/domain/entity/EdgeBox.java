package com.vdc.pdi.device.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 边缘盒子实体
 * 表示边缘计算设备，负责接收视频流并运行算法
 */
@Entity
@Table(name = "edge_box")
@Data
@EqualsAndHashCode(callSuper = true)
public class EdgeBox extends BaseEntity {

    /**
     * 盒子名称
     */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /**
     * IP地址
     */
    @Column(name = "ip_address", nullable = false, length = 15)
    private String ipAddress;

    /**
     * 状态: 0-离线, 1-在线
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 最后心跳时间
     */
    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    /**
     * 软件版本号
     */
    @Column(name = "version", length = 32)
    private String version;

    /**
     * CPU使用率(%)
     */
    @Column(name = "cpu_usage")
    private Double cpuUsage;

    /**
     * 内存使用率(%)
     */
    @Column(name = "memory_usage")
    private Double memoryUsage;

    /**
     * 磁盘使用率(%)
     */
    @Column(name = "disk_usage")
    private Double diskUsage;

    /**
     * 是否在线
     */
    @Transient
    public boolean isOnline() {
        return status != null && status == 1;
    }

    /**
     * 获取状态文本
     */
    @Transient
    public String getStatusText() {
        return status != null && status == 1 ? "在线" : "离线";
    }
}
