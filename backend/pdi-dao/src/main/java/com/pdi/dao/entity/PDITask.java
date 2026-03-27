package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PDI作业实体
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("pdi_task")
public class PDITask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业编号
     */
    private String taskNo;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 持续时间(秒)
     */
    private Integer durationSeconds;

    /**
     * 标准持续时间(秒)
     */
    private Integer standardDuration;

    /**
     * 进入状态序列
     */
    private String enterStateSeq;

    /**
     * 离开状态序列
     */
    private String exitStateSeq;

    /**
     * 作业状态: 1-进行中, 2-已完成, 3-异常中断
     */
    private Integer taskStatus;

    /**
     * 作业结果: 1-合格, 2-超时, 3-异常
     */
    private Integer taskResult;

    /**
     * 开始图片URL
     */
    private String startImageUrl;

    /**
     * 结束图片URL
     */
    private String endImageUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 备注
     */
    private String remark;

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
