package com.pdi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 盒子DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 盒子ID
     */
    private Long id;

    /**
     * 盒子编码
     */
    @NotBlank(message = "盒子编码不能为空")
    private String boxCode;

    /**
     * 盒子名称
     */
    @NotBlank(message = "盒子名称不能为空")
    private String boxName;

    /**
     * 站点ID
     */
    @NotNull(message = "站点ID不能为空")
    private Long siteId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 描述
     */
    private String description;
}
