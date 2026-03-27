package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站点信息VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 站点ID
     */
    private Long id;

    /**
     * 站点编码
     */
    private String siteCode;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 站点地址
     */
    private String address;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 盒子数量
     */
    private Integer boxCount;

    /**
     * 通道数量
     */
    private Integer channelCount;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
