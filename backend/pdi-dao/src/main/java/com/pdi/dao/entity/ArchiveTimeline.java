package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 档案时间线实体
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("archive_timeline")
public class ArchiveTimeline implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 档案日期
     */
    private LocalDate archiveDate;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 总作业数
     */
    private Integer totalTasks;

    /**
     * 已完成作业数
     */
    private Integer completedTasks;

    /**
     * 超时作业数
     */
    private Integer timeoutTasks;

    /**
     * 总报警数
     */
    private Integer totalAlarms;

    /**
     * 吸烟报警数
     */
    private Integer smokingAlarms;

    /**
     * 时间线数据(JSONB)
     */
    private JsonNode timelineData;

    /**
     * 归档文件URL
     */
    private String archiveFileUrl;

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
