package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通道预览VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * WebSocket-FLV地址
     */
    private String wsFlvUrl;

    /**
     * WebRTC地址
     */
    private String webrtcUrl;

    /**
     * HLS地址
     */
    private String hlsUrl;

    /**
     * 过期时间（秒）
     */
    private Integer expireSeconds;
}
