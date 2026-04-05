package com.vdc.pdi.device.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备状态概览响应
 */
@Data
public class DeviceOverviewResponse {

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 站点名称
     */
    private String siteName;

    // ==================== 盒子统计 ====================

    /**
     * 盒子总数
     */
    private Integer totalBoxes;

    /**
     * 在线盒子数
     */
    private Integer onlineBoxes;

    /**
     * 离线盒子数
     */
    private Integer offlineBoxes;

    /**
     * 盒子在线率
     */
    private Double boxOnlineRate;

    // ==================== 通道统计 ====================

    /**
     * 通道总数
     */
    private Integer totalChannels;

    /**
     * 在线通道数
     */
    private Integer onlineChannels;

    /**
     * 离线通道数
     */
    private Integer offlineChannels;

    /**
     * 通道在线率
     */
    private Double channelOnlineRate;

    // ==================== 算法统计 ====================

    /**
     * 抽烟检测通道数
     */
    private Integer smokeChannels;

    /**
     * PDI左前门通道数
     */
    private Integer pdiLeftFrontChannels;

    /**
     * PDI左后门通道数
     */
    private Integer pdiLeftRearChannels;

    /**
     * PDI滑移门通道数
     */
    private Integer pdiSlideChannels;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
