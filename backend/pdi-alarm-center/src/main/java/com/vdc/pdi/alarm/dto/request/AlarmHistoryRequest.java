package com.vdc.pdi.alarm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 历史预警查询请求
 */
@Data
@Schema(description = "历史预警查询请求")
public class AlarmHistoryRequest {

    @Schema(description = "页码", example = "1")
    @Min(1)
    private Integer page = 1;

    @Schema(description = "每页条数", example = "20")
    @Min(1)
    @Max(100)
    private Integer size = 20;

    @Schema(description = "站点ID")
    private Long siteId;

    @Schema(description = "报警类型：0-抽烟，1-PDI违规")
    private Integer type;

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "状态：0-未处理，1-已处理，2-误报")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
