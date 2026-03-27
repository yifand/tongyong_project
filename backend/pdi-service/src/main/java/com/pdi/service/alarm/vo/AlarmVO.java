package com.pdi.service.alarm.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class AlarmVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警ID
     */
    private Long id;

    /**
     * 报警编号
     */
    private String alarmNo;

    /**
     * 报警类型: 1-PDI超时, 2-违规吸烟, 3-门异常开启, 4-人员异常
     */
    private Integer alarmType;

    /**
     * 报警类型名称
     */
    private String alarmTypeName;

    /**
     * 报警级别: 1-低, 2-中, 3-高, 4-紧急
     */
    private Integer alarmLevel;

    /**
     * 报警级别名称
     */
    private String alarmLevelName;

    /**
     * 报警标题
     */
    private String alarmTitle;

    /**
     * 报警描述
     */
    private String alarmDesc;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 盒子名称
     */
    private String boxName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

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
     * 处理状态: 0-未处理, 1-已确认, 2-已处理, 3-误报
     */
    private Integer handleStatus;

    /**
     * 处理状态名称
     */
    private String handleStatusName;

    /**
     * 置信度
     */
    private Double confidence;

}
