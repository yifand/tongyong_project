package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 状态流实体（分区表）
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("state_stream")
public class StateStream implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态流ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件发生时间（分区键）
     */
    private LocalDateTime eventTime;

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
     * 门是否开启: 0-关闭, 1-开启
     */
    private Integer doorOpen;

    /**
     * 是否有人: 0-无人, 1-有人
     */
    private Integer personPresent;

    /**
     * 人员进出状态: 0-无, 1-进入, 2-离开
     */
    private Integer personEnteringExiting;

    /**
     * 状态编码: S1,S3,S5,S7,S8
     */
    private String stateCode;

    /**
     * 门状态置信度
     */
    private BigDecimal doorConfidence;

    /**
     * 人员状态置信度
     */
    private BigDecimal personConfidence;

    /**
     * 帧ID
     */
    private Long frameId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 是否已处理: 0-未处理, 1-已处理
     */
    private Integer processed;

}
