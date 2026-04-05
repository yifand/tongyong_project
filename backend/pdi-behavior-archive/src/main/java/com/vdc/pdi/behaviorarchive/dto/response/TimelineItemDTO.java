package com.vdc.pdi.behaviorarchive.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间线节点DTO
 */
@Data
@Builder
@Schema(description = "时间线节点")
public class TimelineItemDTO {

    @Schema(description = "顺序号")
    private Integer seq;

    @Schema(description = "事件时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;

    @Schema(description = "行为描述")
    private String action;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "时间节点类型：start-开始，process-进行中，end-结束")
    private String nodeType;

    @Schema(description = "相对开始时间的偏移（秒）")
    private Integer offsetSeconds;
}
