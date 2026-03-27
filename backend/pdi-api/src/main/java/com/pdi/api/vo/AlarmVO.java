package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警信息VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警ID
     */
    private Long id;

    /**
     * 预警编号
     */
    private String alarmNo;

    /**
     * 预警类型（1-PDI超时，2-违规吸烟，3-门异常开启，4-人员异常）
     */
    private Integer alarmType;

    /**
     * 预警类型名称
     */
    private String alarmTypeName;

    /**
     * 预警级别（1-低，2-中，3-高，4-紧急）
     */
    private Integer alarmLevel;

    /**
     * 预警级别名称
     */
    private String alarmLevelName;

    /**
     * 预警标题
     */
    private String alarmTitle;

    /**
     * 预警描述
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
     * 预警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 报警图片URL
     */
    private String imageUrl;

    /**
     * 报警视频URL
     */
    private String videoUrl;

    /**
     * 处理状态（0-未处理，1-已确认，2-已处理，3-误报）
     */
    private Integer handleStatus;

    /**
     * 处理状态名称
     */
    private String handleStatusName;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人ID
     */
    private Long handleUserId;

    /**
     * 处理人名称
     */
    private String handleUserName;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
