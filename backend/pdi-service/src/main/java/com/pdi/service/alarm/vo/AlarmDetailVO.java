package com.pdi.service.alarm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 预警详情VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmDetailVO extends AlarmVO {

    private static final long serialVersionUID = 1L;

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
     * 扩展数据
     */
    private String extraData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
