package com.vdc.pdi.auth.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 */
@Entity
@Table(name = "sys_user")
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar", length = 200)
    private String avatar;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 0-禁用, 1-启用

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "data_scope")
    private Integer dataScope; // 数据权限范围

    @Column(name = "login_fail_count")
    private Integer loginFailCount = 0;

    @Column(name = "login_lock_time")
    private LocalDateTime loginLockTime;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "update_by")
    private Long updateBy;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    // Getters and Setters
    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        setName(realName);
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

    public Integer getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public LocalDateTime getLoginLockTime() {
        return loginLockTime;
    }

    public void setLoginLockTime(LocalDateTime loginLockTime) {
        this.loginLockTime = loginLockTime;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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
        setLastLoginAt(lastLoginTime);
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    /**
     * 获取创建时间（从BaseEntity）
     */
    public LocalDateTime getCreatedAt() {
        return super.getCreatedAt();
    }

    /**
     * @deprecated 使用 {@link #getCreatedAt()} 替代
     */
    @Deprecated
    public LocalDateTime getCreateTime() {
        return getCreatedAt();
    }

    /**
     * 获取更新时间（从BaseEntity）
     */
    public LocalDateTime getUpdatedAt() {
        return super.getUpdatedAt();
    }

    /**
     * @deprecated 使用 {@link #getUpdatedAt()} 替代
     */
    @Deprecated
    public LocalDateTime getUpdateTime() {
        return getUpdatedAt();
    }

    /**
     * 获取删除时间（从BaseEntity）
     */
    public LocalDateTime getDeletedAt() {
        return super.getDeletedAt();
    }

    /**
     * @deprecated 使用 {@link #getDeletedAt()} 替代
     */
    @Deprecated
    public Boolean getDeleted() {
        return super.isDeleted();
    }

    /**
     * 获取创建人（从BaseEntity）
     */
    public Long getCreatedBy() {
        return super.getCreatedBy();
    }

    /**
     * @deprecated 使用 {@link #getCreatedBy()} 替代
     */
    @Deprecated
    public Long getCreateBy() {
        return getCreatedBy();
    }

    /**
     * 获取站点ID（从BaseEntity）
     */
    public Long getSiteId() {
        return super.getSiteId();
    }

    public void setSiteId(Long siteId) {
        super.setSiteId(siteId);
    }
}
