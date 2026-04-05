package com.vdc.pdi.device.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通道创建/更新请求
 */
@Data
public class ChannelRequest {

    /**
     * 通道名称
     */
    @NotBlank(message = "通道名称不能为空")
    @Size(max = 64, message = "通道名称长度不能超过64")
    private String name;

    /**
     * 所属盒子ID
     */
    @NotNull(message = "所属盒子不能为空")
    private Long boxId;

    /**
     * 所属站点ID
     */
    @NotNull(message = "所属站点不能为空")
    private Long siteId;

    /**
     * 通道类型：0-视频流，1-抓拍机
     */
    @NotNull(message = "通道类型不能为空")
    @Min(value = 0, message = "通道类型只能是0(视频流)或1(抓拍机)")
    @Max(value = 1, message = "通道类型只能是0(视频流)或1(抓拍机)")
    private Integer type;

    /**
     * 算法类型：smoke, pdi_left_front, pdi_left_rear, pdi_slide
     */
    @Size(max = 64, message = "算法类型长度不能超过64")
    private String algorithmType;

    /**
     * RTSP地址
     */
    @Size(max = 256, message = "RTSP地址长度不能超过256")
    private String rtspUrl;
}
