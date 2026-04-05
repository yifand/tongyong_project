package com.vdc.pdi.ruleengine.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业时长计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDurationResult {

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 是否为合并计算
     */
    private boolean merged;

    /**
     * 子任务列表（合并时有效）
     */
    private List<SingleTaskInfo> subTasks;

    /**
     * 进入时间
     */
    private LocalDateTime entryTime;

    /**
     * 离开时间
     */
    private LocalDateTime exitTime;

    /**
     * 实际时长(秒)
     */
    private long actualSeconds;

    /**
     * 标准时长(秒)
     */
    private long standardSeconds;

    /**
     * 偏差时长(秒)
     */
    private long deviationSeconds;

    /**
     * 获取实际时长格式化字符串
     */
    public String getFormattedActualDuration() {
        long minutes = actualSeconds / 60;
        long seconds = actualSeconds % 60;
        return String.format("%d分%d秒", minutes, seconds);
    }

    /**
     * 获取偏差时长格式化字符串
     */
    public String getFormattedDeviation() {
        long minutes = Math.abs(deviationSeconds) / 60;
        long seconds = Math.abs(deviationSeconds) % 60;
        String sign = deviationSeconds >= 0 ? "+" : "-";
        return String.format("%s%d分%d秒", sign, minutes, seconds);
    }
}
