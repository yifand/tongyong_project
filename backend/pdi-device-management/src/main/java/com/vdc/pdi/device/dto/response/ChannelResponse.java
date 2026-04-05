package com.vdc.pdi.device.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通道响应
 */
@Data
public class ChannelResponse {

    /**
     * 通道ID
     */
    private Long id;

    /**
     * 通道名称
     */
    private String name;

    /**
     * 盒子ID
     */
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
     * 类型：0-视频流，1-抓拍机
     */
    private Integer type;

    /**
     * 类型文本
     */
    private String typeText;

    /**
     * 状态：0-离线，1-在线
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 算法类型
     */
    private String algorithmType;

    /**
     * 算法类型文本
     */
    private String algorithmTypeText;

    /**
     * RTSP地址（脱敏显示）
     */
    private String rtspUrl;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
