package com.vdc.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysUserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    private Long siteId;

    @NotNull(message = "Status is required")
    private Integer status;
}
