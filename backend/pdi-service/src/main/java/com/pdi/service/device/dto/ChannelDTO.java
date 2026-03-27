package com.pdi.service.device.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通道DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class ChannelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通道ID
     */
    private Long id;

    /**
     * 通道编码
     */
    @NotBlank(message = "通道编码不能为空")
    private String channelCode;

    /**
     * 通道名称
     */
    @NotBlank(message = "通道名称不能为空")
    private String channelName;

    /**
     * 盒子ID
     */
    @NotNull(message = "盒子ID不能为空")
    private Long boxId;

    /**
     * 盒子名称
     */
    private String boxName;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 通道类型: 1-PDI检测, 2-吸烟检测
     */
    @NotNull(message = "通道类型不能为空")
    private Integer channelType;

    /**
     * 通道类型名称
     */
    private String channelTypeName;

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
     * 状态名称
     */
    private String statusName;

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
    private LocalDateTime createdAt;

}
