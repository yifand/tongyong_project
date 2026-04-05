package com.vdc.pdi.behaviorarchive.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案详情响应
 */
@Data
@Schema(description = "档案详情响应")
public class ArchiveDetailResponse {

    @Schema(description = "档案ID")
    private Long id;

    @Schema(description = "PDI任务ID")
    private Long pdiTaskId;

    @Schema(description = "通道信息")
    private ChannelInfo channel;

    @Schema(description = "站点信息")
    private SiteInfo site;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "预估检查时间（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "实际检查时间（秒），进行中为null")
    private Integer actualDuration;

    @Schema(description = "状态：0-进行中，1-达标，2-未达标")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "时间线数据")
    private List<TimelineItemDTO> timeline;

    @Data
    @Schema(description = "通道信息")
    public static class ChannelInfo {
        private Long id;
        private String name;
        private String algorithmType;
    }

    @Data
    @Schema(description = "站点信息")
    public static class SiteInfo {
        private Long id;
        private String name;
        private String code;
    }
}
