package com.vdc.pdi.ruleengine.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 单任务信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleTaskInfo {

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 进入时间
     */
    private LocalDateTime entryTime;

    /**
     * 离开时间
     */
    private LocalDateTime exitTime;

    /**
     * 持续时长(秒)
     */
    private Long durationSeconds;

    /**
     * 计算时长
     */
    public Long getDurationSeconds() {
        if (entryTime != null && exitTime != null) {
            return java.time.Duration.between(entryTime, exitTime).getSeconds();
        }
        return durationSeconds;
    }
}
