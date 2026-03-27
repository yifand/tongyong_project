package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预警详情VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmDetailVO implements Serializable {

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
     * 预警类型
     */
    private Integer alarmType;

    /**
     * 预警类型名称
     */
    private String alarmTypeName;

    /**
     * 预警级别
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
     * PDI任务编号
     */
    private String pdiTaskNo;

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
     * 图片列表
     */
    private List<String> images;

    /**
     * 处理状态
     */
    private Integer handleStatus;

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
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 扩展数据
     */
    private Map<String, Object> extraData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
