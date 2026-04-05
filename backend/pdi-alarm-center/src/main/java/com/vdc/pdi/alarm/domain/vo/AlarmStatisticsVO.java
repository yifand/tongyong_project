package com.vdc.pdi.alarm.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报警统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmStatisticsVO {

    /**
     * 总数
     */
    private Long total;

    /**
     * 未处理数
     */
    private Long unprocessed;

    /**
     * 已处理数
     */
    private Long processed;
}
