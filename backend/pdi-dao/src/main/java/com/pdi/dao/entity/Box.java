package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 盒子设备实体
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("box")
public class Box implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 盒子编号
     */
    private String boxCode;

    /**
     * 盒子名称
     */
    private String boxName;

    /**
     * 所属点位ID
     */
    private Long siteId;

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
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * CPU使用率(%)
     */
    private java.math.BigDecimal cpuUsage;

    /**
     * 内存使用率(%)
     */
    private java.math.BigDecimal memoryUsage;

    /**
     * 磁盘使用率(%)
     */
    private java.math.BigDecimal diskUsage;

    /**
     * GPU使用率(%)
     */
    private java.math.BigDecimal gpuUsage;

    /**
     * 软件版本
     */
    private String softwareVersion;

    /**
     * 算法版本
     */
    private String algorithmVersion;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 是否删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
