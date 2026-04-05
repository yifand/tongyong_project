package com.vdc.pdi.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 盒子创建/更新请求
 */
@Data
public class BoxRequest {

    /**
     * 盒子名称
     */
    @NotBlank(message = "盒子名称不能为空")
    @Size(max = 64, message = "盒子名称长度不能超过64")
    private String name;

    /**
     * IP地址
     */
    @NotBlank(message = "IP地址不能为空")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "IP地址格式不正确")
    private String ipAddress;

    /**
     * 所属站点ID
     */
    @NotNull(message = "所属站点不能为空")
    private Long siteId;

    /**
     * 版本号
     */
    @Size(max = 32, message = "版本号长度不能超过32")
    private String version;
}
