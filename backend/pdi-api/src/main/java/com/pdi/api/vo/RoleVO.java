package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色信息VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 描述
     */
    private String description;

    /**
     * 数据范围（1-全部数据，2-本站点数据）
     */
    private Integer dataScope;

    /**
     * 数据范围名称
     */
    private String dataScopeName;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 用户数量
     */
    private Integer userCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
