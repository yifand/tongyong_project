package com.vdc.pdi.device.dto.response;

import lombok.Data;

/**
 * 站点设备状态响应
 */
@Data
public class SiteDeviceStatusResponse {

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 站点名称
     */
    private String siteName;

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
}
