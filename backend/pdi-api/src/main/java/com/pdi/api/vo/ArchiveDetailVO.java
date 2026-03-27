package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案详情VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
    private Long id;

    /**
     * 任务编号
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
     * 作业时长（分钟）
     */
    private Long duration;

    /**
     * 标准作业时长
     */
    private Integer standardDuration;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 作业结果
     */
    private Integer taskResult;

    /**
     * 进入状态序列
     */
    private List<String> enterStateSeq;

    /**
     * 离开状态序列
     */
    private List<String> exitStateSeq;

    /**
     * 开始图片URL
     */
    private String startImageUrl;

    /**
     * 结束图片URL
     */
    private String endImageUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 报警列表
     */
    private List<ArchiveAlarmVO> alarms;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 档案报警VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchiveAlarmVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 报警ID
         */
        private Long id;

        /**
         * 报警类型
         */
        private Integer alarmType;

        /**
         * 报警类型名称
         */
        private String alarmTypeName;

        /**
         * 报警时间
         */
        private LocalDateTime alarmTime;
    }
}
