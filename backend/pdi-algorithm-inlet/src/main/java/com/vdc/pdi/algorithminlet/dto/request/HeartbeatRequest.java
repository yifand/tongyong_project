package com.vdc.pdi.algorithminlet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 心跳请求DTO
 * 接收边缘盒子心跳数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "心跳请求")
public class HeartbeatRequest {

    @NotBlank(message = "盒子ID不能为空")
    @Schema(description = "盒子ID", example = "BOX_001")
    private String boxId;

    @NotNull(message = "时间戳不能为空")
    @Schema(description = "心跳时间戳", example = "2026-03-25T10:23:15.000+08:00")
    private LocalDateTime timestamp;

    @DecimalMin(value = "0.0", message = "CPU使用率不能小于0")
    @DecimalMax(value = "100.0", message = "CPU使用率不能大于100")
    @Schema(description = "CPU使用率", example = "45.2")
    private Double cpuUsage;

    @DecimalMin(value = "0.0", message = "内存使用率不能小于0")
    @DecimalMax(value = "100.0", message = "内存使用率不能大于100")
    @Schema(description = "内存使用率", example = "62.1")
    private Double memoryUsage;

    @DecimalMin(value = "0.0", message = "磁盘使用率不能小于0")
    @DecimalMax(value = "100.0", message = "磁盘使用率不能大于100")
    @Schema(description = "磁盘使用率", example = "78.5")
    private Double diskUsage;

    @Schema(description = "软件版本", example = "v2.1.0")
    private String version;
}
