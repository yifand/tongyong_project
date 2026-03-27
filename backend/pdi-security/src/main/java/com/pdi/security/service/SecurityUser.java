package com.pdi.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 安全用户封装类
 * <p>
 * 实现Spring Security的UserDetails接口，封装用户认证和授权信息
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class SecurityUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 角色编码
     */
    private String role;

    /**
     * 用户状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 权限列表（Spring Security格式）
     */
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 是否超级管理员
     */
    @JsonIgnore
    private boolean superAdmin;

    public SecurityUser() {
        this.permissions = new HashSet<>();
    }

    public SecurityUser(Long userId, String username, String password, 
                       Long siteId, String role, Integer status) {
        this();
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.siteId = siteId;
        this.role = role;
        this.status = status;
        this.superAdmin = "SUPER_ADMIN".equals(role) || "admin".equals(role);
    }

    /**
     * 设置权限列表并转换为GrantedAuthority
     */
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.authorities = this.permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取权限列表
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            return new HashSet<>();
        }
        return authorities;
    }

    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return superAdmin || "SUPER_ADMIN".equals(role) || "ADMIN".equals(role);
    }

    /**
     * 账号是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用
     */
    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查是否有指定权限
     *
     * @param permission 权限编码
     * @return 是否有权限
     */
    public boolean hasPermission(String permission) {
        if (isSuperAdmin()) {
            return true;
        }
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 检查是否有指定角色
     *
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    public boolean hasRole(String roleCode) {
        return role != null && role.equals(roleCode);
    }

    /**
     * 获取用户状态描述
     */
    public String getStatusText() {
        return status != null && status == 1 ? "启用" : "禁用";
    }
}
