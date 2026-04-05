package com.vdc.pdi.alarm.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警响应
 */
@Data
@Schema(description = "报警响应")
public class AlarmResponse {

    @Schema(description = "报警ID")
    private Long id;

    @Schema(description = "报警类型：0-抽烟，1-PDI违规")
    private Integer type;

    @Schema(description = "报警类型名称")
    private String typeName;

    @Schema(description = "站点ID")
    private Long siteId;

    @Schema(description = "站点名称")
    private String siteName;

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "通道名称")
    private String channelName;

    @Schema(description = "报警时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;

    @Schema(description = "地点描述")
    private String location;

    @Schema(description = "面部截图URL")
    private String faceImageUrl;

    @Schema(description = "场景截图URL")
    private String sceneImageUrl;

    @Schema(description = "状态：0-未处理，1-已处理，2-误报")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "扩展信息")
    private AlarmExtraInfo extraInfo;

    @Schema(description = "处理人ID")
    private Long processorId;

    @Schema(description = "处理人姓名")
    private String processorName;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

    @Schema(description = "处理备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
