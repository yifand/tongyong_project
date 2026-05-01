package com.vdc.pdi.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应DTO
 */
public class UserResponse {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Long deptId;
    private Integer dataScope;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private List<String> roleCodes;
    private List<String> roleNames;

    // 兼容旧字段
    /**
     * @deprecated 使用 {@link #name} 替代
     */
    @Deprecated
    @JsonAlias("name")
    private String realName;

    /**
     * @deprecated 使用 {@link #lastLoginAt} 替代
     */
    @Deprecated
    @JsonAlias("lastLoginAt")
    private LocalDateTime lastLoginTime;

    /**
     * @deprecated 使用 {@link #createdAt} 替代
     */
    @Deprecated
    @JsonAlias("createdAt")
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.realName = name; // 保持同步
    }

    /**
     * @deprecated 使用 {@link #getName()} 替代
     */
    @Deprecated
    public String getRealName() {
        return getName();
    }

    /**
     * @deprecated 使用 {@link #setName(String)} 替代
     */
    @Deprecated
    public void setRealName(String realName) {
        this.realName = realName;
        this.name = realName; // 保持同步
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getDataScope() {
        return dataScope;
    }

    public void setDataScope(Integer dataScope) {
        this.dataScope = dataScope;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        this.lastLoginTime = lastLoginAt; // 保持同步
    }

    /**
     * @deprecated 使用 {@link #getLastLoginAt()} 替代
     */
    @Deprecated
    public LocalDateTime getLastLoginTime() {
        return getLastLoginAt();
    }

    /**
     * @deprecated 使用 {@link #setLastLoginAt(LocalDateTime)} 替代
     */
    @Deprecated
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
        this.lastLoginAt = lastLoginTime; // 保持同步
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
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

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }
}
