package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报警记录实体（分区表）
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("alarm")
public class Alarm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报警ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报警时间（分区键）
     */
    private LocalDateTime alarmTime;

    /**
     * 报警类型: 1-PDI超时, 2-违规吸烟, 3-门异常开启, 4-人员异常
     */
    private Integer alarmType;

    /**
     * 报警级别: 1-低, 2-中, 3-高, 4-紧急
     */
    private Integer alarmLevel;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * PDI作业ID
     */
    private Long pdiTaskId;

    /**
     * 报警标题
     */
    private String alarmTitle;

    /**
     * 报警描述
     */
    private String alarmDesc;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 处理状态: 0-未处理, 1-已确认, 2-已处理, 3-误报
     */
    private Integer handleStatus;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人ID
     */
    private Long handleUserId;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 扩展数据(JSONB)
     */
    private JsonNode extraData;

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
