package com.vdc.pdi.logmanagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统日志响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统日志响应")
public class SystemLogResponse {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "日志级别")
    private String level;

    @Schema(description = "日志级别编码")
    private Integer levelCode;

    @Schema(description = "模块名称")
    private String module;

    @Schema(description = "日志消息")
    private String message;

    @Schema(description = "异常堆栈")
    private String stackTrace;

    @Schema(description = "来源类名")
    private String sourceClass;

    @Schema(description = "来源方法名")
    private String sourceMethod;

    @Schema(description = "线程名称")
    private String threadName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
