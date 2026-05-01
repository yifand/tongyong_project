package com.vdc.pdi.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建角色请求DTO
 */
public class CreateRoleRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String code;

    /**
     * @deprecated 使用 {@link #name} 替代
     */
    @Deprecated
    @JsonAlias("name")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    /**
     * @deprecated 使用 {@link #code} 替代
     */
    @Deprecated
    @JsonAlias("code")
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    private Integer sortOrder = 0;

    private Integer dataScope;

    private List<String> permissions;

    // Getters and Setters
    public String getName() {
        // 兼容旧字段
        if (name == null && roleName != null) {
            return roleName;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.roleName = name; // 保持同步
    }

    /**
     * @deprecated 使用 {@link #getName()} 替代
     */
    @Deprecated
    public String getRoleName() {
        return getName();
    }

    /**
     * @deprecated 使用 {@link #setName(String)} 替代
     */
    @Deprecated
    public void setRoleName(String roleName) {
        this.roleName = roleName;
        this.name = roleName; // 保持同步
    }

    public String getCode() {
        // 兼容旧字段
        if (code == null && roleCode != null) {
            return roleCode;
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.roleCode = code; // 保持同步
    }

    /**
     * @deprecated 使用 {@link #getCode()} 替代
     */
    @Deprecated
    public String getRoleCode() {
        return getCode();
    }

    /**
     * @deprecated 使用 {@link #setCode(String)} 替代
     */
    @Deprecated
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
        this.code = roleCode; // 保持同步
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getDataScope() {
        return dataScope;
    }

    public void setDataScope(Integer dataScope) {
        this.dataScope = dataScope;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
