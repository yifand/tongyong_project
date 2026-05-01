package com.vdc.pdi.logmanagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "操作日志响应")
public class OperationLogResponse {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作类型编码")
    private Integer operationTypeCode;

    @Schema(description = "操作详情")
    private String operationDetail;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "操作结果：0-失败 1-成功")
    private Integer result;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "执行时长(ms)")
    private Long executionTime;

    @Schema(description = "操作时间")
    private LocalDateTime createdAt;
}
