package com.pdi.service.alarm.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预警统计DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class AlarmStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 站点ID
     */
    private Long siteId;

    // ==================== 统计结果 ====================

    /**
     * 总报警数
     */
    private Long totalAlarms;

    /**
     * 未处理报警数
     */
    private Long unhandledAlarms;

    /**
     * 已处理报警数
     */
    private Long handledAlarms;

    /**
     * 误报数
     */
    private Long falseAlarms;

    /**
     * 按类型统计
     */
    private Map<Integer, Long> alarmTypeStats;

    /**
     * 按级别统计
     */
    private Map<Integer, Long> alarmLevelStats;

    /**
     * 按日期统计
     */
    private List<AlarmTrendItem> alarmTrend;

    /**
     * 报警趋势项
     */
    @Data
    public static class AlarmTrendItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 日期
         */
        private String date;

        /**
         * 报警数量
         */
        private Long count;
    }

}
