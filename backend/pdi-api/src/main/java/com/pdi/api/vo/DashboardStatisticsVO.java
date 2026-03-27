package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 工作台统计数据VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 今日报警数
     */
    private Integer todayAlarms;

    /**
     * 未处理报警数
     */
    private Integer unhandledAlarms;

    /**
     * 今日任务数
     */
    private Integer todayTasks;

    /**
     * 超时任务数
     */
    private Integer timeoutTasks;

    /**
     * 在线盒子数
     */
    private Integer onlineBoxes;

    /**
     * 总盒子数
     */
    private Integer totalBoxes;

    /**
     * 在线率
     */
    private Integer onlineRate;

    /**
     * 报警趋势（最近7小时）
     */
    private List<Integer> alarmTrend;

    /**
     * 任务趋势（最近7小时）
     */
    private List<Integer> taskTrend;
}
