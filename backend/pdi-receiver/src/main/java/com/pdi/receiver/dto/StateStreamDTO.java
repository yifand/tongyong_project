package com.pdi.receiver.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 状态流数据DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class StateStreamDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 门状态: 0-关, 1-开
     */
    private Integer doorOpen;

    /**
     * 人员存在状态: 0-无, 1-有
     */
    private Integer personPresent;

    /**
     * 人员进出状态: 0-无, 1-进出中
     */
    private Integer personEnteringExiting;

    /**
     * 状态编码: S1, S3, S5, S7, S8
     */
    private String stateCode;

    /**
     * 门检测置信度
     */
    private BigDecimal doorConfidence;

    /**
     * 人员检测置信度
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

}
