package com.vdc.pdi.auth.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 */
@Entity
@Table(name = "sys_user_role")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 便捷构造函数
    public UserRole() {
    }

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
        this.id = new UserRoleId(user.getId(), role.getId());
    }

    // Getters and Setters
    public UserRoleId getId() {
        return id;
    }

    public void setId(UserRoleId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        setCreatedAt(createTime);
    }

    /**
     * 获取用户ID（便捷方法）
     */
    public Long getUserId() {
        return id != null ? id.getUserId() : null;
    }

    /**
     * 获取角色ID（便捷方法）
     */
    public Long getRoleId() {
        return id != null ? id.getRoleId() : null;
    }
}
