package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 时间线事件VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEventVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    private Long id;

    /**
     * 事件类型（enter-进入，exit-离开，photo-拍照，alarm-报警）
     */
    private String eventType;

    /**
     * 事件类型名称
     */
    private String eventTypeName;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 状态码
     */
    private String stateCode;
}
