package com.vdc.pdi.logmanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志查询请求
 */
@Data
@Schema(description = "系统日志查询请求")
public class SystemLogRequest {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 20;

    @Schema(description = "日志级别：0-DEBUG 1-INFO 2-WARN 3-ERROR")
    private Integer level;

    @Schema(description = "模块名称")
    private String module;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "关键字搜索")
    private String keyword;
}
