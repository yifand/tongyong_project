package com.pdi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 预警导出DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警级别
     */
    private Integer alarmLevel;

    /**
     * 处理状态
     */
    private Integer handleStatus;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 导出格式（excel/csv）
     */
    private String format;
}
