package com.vdc.pdi.behaviorarchive.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案列表响应
 */
@Data
@Schema(description = "档案列表响应")
public class ArchiveResponse {

    @Schema(description = "档案ID")
    private Long id;

    @Schema(description = "PDI任务ID")
    private Long pdiTaskId;

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "通道名称")
    private String channelName;

    @Schema(description = "站点ID")
    private Long siteId;

    @Schema(description = "站点名称")
    private String siteName;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间，进行中为null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "预估检查时间（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "实际检查时间（秒）")
    private Integer actualDuration;

    @Schema(description = "状态：0-进行中，1-达标，2-未达标")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
