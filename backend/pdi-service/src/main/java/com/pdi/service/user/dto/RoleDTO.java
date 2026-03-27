package com.pdi.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 数据范围: 1-全部数据, 2-本站点数据
     */
    private Integer dataScope;

    /**
     * 数据范围名称
     */
    private String dataScopeName;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 用户数量
     */
    private Integer userCount;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
