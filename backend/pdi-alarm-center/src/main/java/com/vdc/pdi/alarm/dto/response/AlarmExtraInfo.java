package com.vdc.pdi.alarm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报警扩展信息
 */
@Data
@Schema(description = "报警扩展信息")
public class AlarmExtraInfo {

    // PDI违规相关
    @Schema(description = "实际时长（秒）")
    private Integer actualDuration;

    @Schema(description = "标准时长（秒）")
    private Integer standardDuration;

    @Schema(description = "偏差时长（秒）")
    private Integer deviation;

    // 抽烟相关
    @Schema(description = "检测置信度")
    private Double confidence;
}
