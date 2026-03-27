package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通道信息VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通道ID
     */
    private Long id;

    /**
     * 通道编码
     */
    private String channelCode;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 盒子ID
     */
    private Long boxId;

    /**
     * 盒子名称
     */
    private String boxName;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 通道类型（1-PDI检测，2-普通监控）
     */
    private Integer channelType;

    /**
     * 通道类型名称
     */
    private String channelTypeName;

    /**
     * 视频流地址
     */
    private String streamUrl;

    /**
     * 摄像头IP
     */
    private String cameraIp;

    /**
     * 摄像头品牌
     */
    private String cameraBrand;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 是否录像（0-否，1-是）
     */
    private Integer isRecording;

    /**
     * 录像保存天数
     */
    private Integer recordingSaveDays;

    /**
     * 算法配置
     */
    private Map<String, Object> algorithmConfig;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
