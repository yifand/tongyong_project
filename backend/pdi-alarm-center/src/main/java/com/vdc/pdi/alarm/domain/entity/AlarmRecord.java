package com.vdc.pdi.alarm.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 报警记录实体
 */
@Entity
@Table(name = "alarm_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmRecord extends BaseEntity {

    /**
     * 报警类型：0-抽烟，1-PDI违规
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 通道ID
     */
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    /**
     * 报警时间
     */
    @Column(name = "alarm_time", nullable = false)
    private LocalDateTime alarmTime;

    /**
     * 地点描述
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * 面部截图URL
     */
    @Column(name = "face_image_url", length = 500)
    private String faceImageUrl;

    /**
     * 场景截图URL
     */
    @Column(name = "scene_image_url", length = 500)
    private String sceneImageUrl;

    /**
     * 状态：0-未处理，1-已处理，2-误报
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 扩展信息（JSON格式）
     */
    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    /**
     * 处理人ID
     */
    @Column(name = "processor_id")
    private Long processorId;

    /**
     * 处理时间
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 处理备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
}
