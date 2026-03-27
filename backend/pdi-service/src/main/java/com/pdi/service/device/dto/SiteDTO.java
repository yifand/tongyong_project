package com.pdi.service.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 监测点位DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class SiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点位ID
     */
    private Long id;

    /**
     * 点位编码
     */
    @NotBlank(message = "点位编码不能为空")
    private String siteCode;

    /**
     * 点位名称
     */
    @NotBlank(message = "点位名称不能为空")
    private String siteName;

    /**
     * 点位位置
     */
    private String location;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态: 0-禁用, 1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

}
