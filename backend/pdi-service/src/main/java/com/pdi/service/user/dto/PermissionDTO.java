package com.pdi.service.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 权限DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
public class PermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限编码
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 权限类型: 1-菜单, 2-按钮, 3-接口
     */
    private Integer permType;

    /**
     * 权限类型名称
     */
    private String permTypeName;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否已选中
     */
    private Boolean checked;

    /**
     * 子权限列表
     */
    private List<PermissionDTO> children;

}
