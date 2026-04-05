package com.vdc.pdi.alarm.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报警创建事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmCreateEvent {

    /**
     * 报警类型
     */
    private Integer type;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 地点描述
     */
    private String location;

    /**
     * 面部截图URL
     */
    private String faceImageUrl;

    /**
     * 场景截图URL
     */
    private String sceneImageUrl;

    /**
     * 扩展信息（JSON格式）
     */
    private String extraInfo;
}
