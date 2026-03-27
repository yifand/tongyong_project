package com.pdi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 站点DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 站点ID
     */
    private Long id;

    /**
     * 站点编码
     */
    @NotBlank(message = "站点编码不能为空")
    private String siteCode;

    /**
     * 站点名称
     */
    @NotBlank(message = "站点名称不能为空")
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
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
