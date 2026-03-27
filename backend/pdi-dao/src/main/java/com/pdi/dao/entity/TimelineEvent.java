package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 时间线事件实体
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("timeline_event")
public class TimelineEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * PDI任务ID
     */
    private Long taskId;

    /**
     * 事件类型: 1-进入, 2-离开, 3-报警, 4-状态变更
     */
    private Integer eventType;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件描述
     */
    private String eventDesc;

    /**
     * 关联图片URL
     */
    private String imageUrl;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}
