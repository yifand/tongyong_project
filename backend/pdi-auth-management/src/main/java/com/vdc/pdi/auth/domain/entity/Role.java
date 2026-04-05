package com.vdc.pdi.auth.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体类
 */
@Entity
@Table(name = "sys_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String roleCode;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 0-禁用, 1-启用

    @Column(name = "data_scope")
    private Integer dataScope; // 数据权限范围: 1-全部, 2-本部门, 3-本部门及子部门, 4-仅本人, 5-自定义

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "update_by")
    private Long updateBy;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "permissions", length = 500)
    private String permissions;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
}
