package com.vdc.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SysRoleRequest {

    @NotBlank(message = "Role code is required")
    private String roleCode;

    @NotBlank(message = "Role name is required")
    private String roleName;

    @NotNull(message = "Permissions are required")
    private List<String> permissions;

    @NotBlank(message = "Data scope is required")
    private String dataScope;
}
