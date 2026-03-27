package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("system_config")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置类型: string, int, float, json, boolean
     */
    private String configType;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 配置分类: general, algorithm, retention, notification
     */
    private String category;

    /**
     * 是否可编辑: 0-否, 1-是
     */
    private Integer isEditable;

    /**
     * 排序号
     */
    private Integer sortOrder;

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

}
