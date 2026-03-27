package com.pdi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 通道DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 通道类型（1-PDI检测，2-普通监控）
     */
    @NotNull(message = "通道类型不能为空")
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
     * 是否录像（0-否，1-是）
     */
    private Integer isRecording;

    /**
     * 录像保存天数
     */
    private Integer recordingSaveDays;

    /**
     * 算法配置
     */
    private Map<String, Object> algorithmConfig;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
