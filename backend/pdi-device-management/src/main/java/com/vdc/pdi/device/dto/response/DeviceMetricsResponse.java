package com.vdc.pdi.device.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备资源使用率响应
 */
@Data
public class DeviceMetricsResponse {

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 盒子名称
     */
    private String boxName;

    /**
     * CPU使用率
     */
    private Double cpuUsage;

    /**
     * CPU使用率文本
     */
    private String cpuUsageText;

    /**
     * 内存使用率
     */
    private Double memoryUsage;

    /**
     * 内存使用率文本
     */
    private String memoryUsageText;

    /**
     * 磁盘使用率
     */
    private Double diskUsage;

    /**
     * 磁盘使用率文本
     */
    private String diskUsageText;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime;

    /**
     * 历史趋势（最近24小时）
     */
    private List<MetricsHistory> history;

    /**
     * 指标历史记录
     */
    @Data
    public static class MetricsHistory {

        /**
         * 时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;

        /**
         * CPU使用率
         */
        private Double cpuUsage;

        /**
         * 内存使用率
         */
        private Double memoryUsage;

        /**
         * 磁盘使用率
         */
        private Double diskUsage;
    }
}
