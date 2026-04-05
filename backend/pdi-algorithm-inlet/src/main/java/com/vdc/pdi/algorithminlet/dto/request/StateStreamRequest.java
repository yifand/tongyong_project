package com.vdc.pdi.algorithminlet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 状态流请求DTO
 * 接收边缘盒子推送的状态三元组流数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "状态流请求")
public class StateStreamRequest {

    @NotBlank(message = "盒子ID不能为空")
    @Schema(description = "盒子ID", example = "BOX_001")
    private String boxId;

    @NotBlank(message = "通道ID不能为空")
    @Schema(description = "通道ID", example = "CH_001")
    private String channelId;

    @NotNull(message = "时间戳不能为空")
    @Schema(description = "事件时间戳", example = "2026-03-25T10:23:15.000+08:00")
    private LocalDateTime timestamp;

    @NotNull(message = "状态数据不能为空")
    @Valid
    @Schema(description = "状态三元组")
    private StateTriple state;

    @NotNull(message = "状态码不能为空")
    @Min(value = 1, message = "状态码必须在1-8之间")
    @Max(value = 8, message = "状态码必须在1-8之间")
    @Schema(description = "状态码: S1=1, S3=3, S5=5, S7=7, S8=8", example = "3")
    private Integer stateCode;

    /**
     * 状态三元组
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "状态三元组")
    public static class StateTriple {

        @NotNull(message = "门状态不能为空")
        @Min(value = 0, message = "门状态只能是0或1")
        @Max(value = 1, message = "门状态只能是0或1")
        @Schema(description = "门状态: 0=关, 1=开", example = "0")
        private Integer doorOpen;

        @NotNull(message = "人员状态不能为空")
        @Min(value = 0, message = "人员状态只能是0或1")
        @Max(value = 1, message = "人员状态只能是0或1")
        @Schema(description = "人员状态: 0=无人, 1=有人", example = "1")
        private Integer personPresent;

        @NotNull(message = "进出状态不能为空")
        @Min(value = 0, message = "进出状态只能是0或1")
        @Max(value = 1, message = "进出状态只能是0或1")
        @Schema(description = "进出状态: 0=未进出, 1=进出中", example = "0")
        private Integer enteringExiting;
    }
}
