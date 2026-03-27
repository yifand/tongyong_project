package com.pdi.receiver.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 报警事件DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class AlarmEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 报警类型: 1-PDI超时, 2-违规吸烟, 3-门异常开启, 4-人员异常
     */
    private Integer alarmType;

    /**
     * 报警级别: 1-低, 2-中, 3-高, 4-紧急
     */
    private Integer alarmLevel;

    /**
     * 报警标题
     */
    private String alarmTitle;

    /**
     * 报警描述
     */
    private String alarmDesc;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 关联PDI任务ID
     */
    private Long pdiTaskId;

    /**
     * 检测置信度
     */
    private Double confidence;

    /**
     * 扩展数据(JSON)
     */
    private String extraData;

}
