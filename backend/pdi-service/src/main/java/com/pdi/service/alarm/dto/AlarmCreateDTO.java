package com.pdi.service.alarm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警创建DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class AlarmCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报警类型: 1-PDI超时, 2-违规吸烟, 3-门异常开启, 4-人员异常
     */
    @NotNull(message = "报警类型不能为空")
    private Integer alarmType;

    /**
     * 报警级别: 1-低, 2-中, 3-高, 4-紧急
     */
    @NotNull(message = "报警级别不能为空")
    private Integer alarmLevel;

    /**
     * 报警标题
     */
    @NotBlank(message = "报警标题不能为空")
    private String alarmTitle;

    /**
     * 报警描述
     */
    private String alarmDesc;

    /**
     * 站点ID
     */
    @NotNull(message = "站点ID不能为空")
    private Long siteId;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * PDI任务ID
     */
    private Long pdiTaskId;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 扩展数据(JSON格式)
     */
    private String extraData;

}
