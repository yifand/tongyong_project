package com.vdc.pdi.algorithminlet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报警事件请求DTO
 * 接收边缘盒子推送的报警事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报警事件请求")
public class AlarmEventRequest {

    @NotBlank(message = "盒子ID不能为空")
    @Schema(description = "盒子ID", example = "BOX_001")
    private String boxId;

    @NotBlank(message = "通道ID不能为空")
    @Schema(description = "通道ID", example = "CH_001")
    private String channelId;

    @NotBlank(message = "报警类型不能为空")
    @Pattern(regexp = "SMOKE|PDI_VIOLATION", message = "报警类型只能是SMOKE或PDI_VIOLATION")
    @Schema(description = "报警类型: SMOKE=抽烟, PDI_VIOLATION=PDI违规", example = "SMOKE")
    private String alarmType;

    @NotNull(message = "时间戳不能为空")
    @Schema(description = "报警时间戳", example = "2026-03-25T10:23:15.000+08:00")
    private LocalDateTime timestamp;

    @Schema(description = "报警图片URL", example = "http://minio/bucket/alarm/xxx.jpg")
    private String imageUrl;

    @DecimalMin(value = "0.0", message = "置信度不能小于0")
    @DecimalMax(value = "1.0", message = "置信度不能大于1")
    @Schema(description = "置信度", example = "0.92")
    private Double confidence;

    @Schema(description = "报警位置", example = "休息区A")
    private String location;
}
