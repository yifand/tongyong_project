package com.vdc.pdi.behaviorarchive.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案列表查询请求
 */
@Data
@Schema(description = "档案列表查询请求")
public class ArchiveListRequest {

    @Schema(description = "站点ID")
    private Long siteId;

    @Schema(description = "档案状态：0-进行中，1-达标，2-未达标")
    @Min(value = 0, message = "状态码不能小于0")
    @Max(value = 2, message = "状态码不能大于2")
    private Integer status;

    @Schema(description = "开始时间-起始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeFrom;

    @Schema(description = "开始时间-结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeTo;

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "页码，默认1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页条数，默认20，最大100")
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能大于100")
    private Integer size = 20;
}
