package com.vdc.pdi.logmanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志查询请求
 */
@Data
@Schema(description = "操作日志查询请求")
public class OperationLogRequest {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 20;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "操作类型：1-登录 2-登出 3-配置修改 4-用户管理 5-角色管理 6-数据导出 7-报警处理 8-设备管理 9-阈值配置 99-其他")
    private Integer operationType;

    @Schema(description = "操作结果：0-失败 1-成功")
    private Integer result;
}
