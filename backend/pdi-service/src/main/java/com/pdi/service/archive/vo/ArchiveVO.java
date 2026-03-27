package com.pdi.service.archive.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class ArchiveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
    private Long id;

    /**
     * 作业编号
     */
    private String taskNo;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 作业时长(分钟)
     */
    private Long duration;

    /**
     * 标准时长(分钟)
     */
    private Integer standardDuration;

    /**
     * 作业状态: 1-进行中, 2-已完成
     */
    private Integer taskStatus;

    /**
     * 作业状态名称
     */
    private String taskStatusName;

    /**
     * 作业结果: 1-合格, 2-超时, 3-异常
     */
    private Integer taskResult;

    /**
     * 作业结果名称
     */
    private String taskResultName;

    /**
     * 报警数量
     */
    private Integer alarmCount;

    /**
     * 开始图片URL
     */
    private String startImageUrl;

    /**
     * 结束图片URL
     */
    private String endImageUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
