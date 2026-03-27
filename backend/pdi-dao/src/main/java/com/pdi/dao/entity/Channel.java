package com.pdi.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通道实体
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@TableName("channel")
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通道ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通道编码
     */
    private String channelCode;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 通道类型: 1-PDI检测, 2-吸烟检测
     */
    private Integer channelType;

    /**
     * 视频流地址
     */
    private String streamUrl;

    /**
     * 摄像头IP
     */
    private String cameraIp;

    /**
     * 摄像头品牌
     */
    private String cameraBrand;

    /**
     * 算法配置(JSONB)
     */
    private JsonNode algorithmConfig;

    /**
     * 状态: 0-禁用, 1-启用, 2-故障
     */
    private Integer status;

    /**
     * 是否录像: 0-否, 1-是
     */
    private Integer isRecording;

    /**
     * 录像保存天数
     */
    private Integer recordingSaveDays;

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
