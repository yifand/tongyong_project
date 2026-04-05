package com.vdc.pdi.alarm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报警统计响应
 */
@Data
@Schema(description = "报警统计响应")
public class AlarmStatisticsResponse {

    @Schema(description = "今日报警总数")
    private Long total;

    @Schema(description = "未处理数")
    private Long unprocessed;

    @Schema(description = "已处理数")
    private Long processed;

    @Schema(description = "处理率")
    private String processRate;
}
