package com.vdc.pdi.device.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频通道实体
 * 表示边缘盒子下的视频通道，关联算法配置
 */
@Entity
@Table(name = "channel")
@Data
@EqualsAndHashCode(callSuper = true)
public class Channel extends BaseEntity {

    /**
     * 所属盒子ID
     */
    @Column(name = "box_id", nullable = false)
    private Long boxId;

    /**
     * 通道名称
     */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /**
     * 通道类型: 0-视频流, 1-抓拍机
     */
    @Column(name = "type", nullable = false)
    private Integer type = 0;

    /**
     * 状态: 0-离线, 1-在线
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 算法类型: smoke, pdi_left_front, pdi_left_rear, pdi_slide
     */
    @Column(name = "algorithm_type", length = 64)
    private String algorithmType;

    /**
     * RTSP地址(加密存储)
     */
    @Column(name = "rtsp_url", length = 512)
    private String rtspUrl;

    /**
     * 是否在线
     */
    @Transient
    public boolean isOnline() {
        return status != null && status == 1;
    }

    /**
     * 获取状态文本
     */
    @Transient
    public String getStatusText() {
        return status != null && status == 1 ? "在线" : "离线";
    }

    /**
     * 获取类型文本
     */
    @Transient
    public String getTypeText() {
        return type != null && type == 1 ? "抓拍机" : "视频流";
    }
}
