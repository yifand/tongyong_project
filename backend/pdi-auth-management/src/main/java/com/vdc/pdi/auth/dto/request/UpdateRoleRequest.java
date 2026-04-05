package com.vdc.pdi.auth.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 更新角色请求DTO
 */
public class UpdateRoleRequest {

    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    private Integer sortOrder;

    private Integer dataScope;

    private List<String> permissions;

    // Getters and Setters
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
