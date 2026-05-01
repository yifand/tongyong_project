package com.vdc.pdi.auth.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体类
 */
@Entity
@Table(name = "sys_role")
@EntityListeners(AuditingEntityListener.class)
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 0-禁用, 1-启用

    @Column(name = "data_scope")
    private Integer dataScope; // 数据权限范围: 1-全部, 2-本部门, 3-本部门及子部门, 4-仅本人, 5-自定义

    @Column(name = "update_by")
    private Long updateBy;

    @Column(name = "permissions", length = 500)
    private String permissions;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    // Getters and Setters
    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
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
    public String getRoleName() {
        return getName();
    }

    /**
     * @deprecated 使用 {@link #setName(String)} 替代
     */
    @Deprecated
    public void setRoleName(String roleName) {
        setName(roleName);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
        setCode(roleCode);
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

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
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
     * 获取站点ID（从BaseEntity）
     */
    public Long getSiteId() {
        return super.getSiteId();
    }

    public void setSiteId(Long siteId) {
        super.setSiteId(siteId);
    }
}
