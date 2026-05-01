package com.vdc.pdi.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应DTO
 */
public class RoleResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Integer dataScope;
    private List<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 兼容旧字段
    /**
     * @deprecated 使用 {@link #name} 替代
     */
    @Deprecated
    @JsonAlias("name")
    private String roleName;

    /**
     * @deprecated 使用 {@link #code} 替代
     */
    @Deprecated
    @JsonAlias("code")
    private String roleCode;

    /**
     * @deprecated 使用 {@link #createdAt} 替代
     */
    @Deprecated
    @JsonAlias("createdAt")
    private LocalDateTime createTime;

    /**
     * @deprecated 使用 {@link #updatedAt} 替代
     */
    @Deprecated
    @JsonAlias("updatedAt")
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.createTime = createdAt; // 保持同步
    }

    /**
     * @deprecated 使用 {@link #getCreatedAt()} 替代
     */
    @Deprecated
    public LocalDateTime getCreateTime() {
        return getCreatedAt();
    }

    /**
     * @deprecated 使用 {@link #setCreatedAt(LocalDateTime)} 替代
     */
    @Deprecated
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        this.createdAt = createTime; // 保持同步
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        this.updateTime = updatedAt; // 保持同步
    }

    /**
     * @deprecated 使用 {@link #getUpdatedAt()} 替代
     */
    @Deprecated
    public LocalDateTime getUpdateTime() {
        return getUpdatedAt();
    }

    /**
     * @deprecated 使用 {@link #setUpdatedAt(LocalDateTime)} 替代
     */
    @Deprecated
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        this.updatedAt = updateTime; // 保持同步
    }
}
