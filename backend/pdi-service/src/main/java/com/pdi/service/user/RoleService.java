package com.pdi.service.user;

import com.pdi.service.user.dto.PermissionDTO;
import com.pdi.service.user.dto.RoleDTO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param dto 角色信息
     * @return 创建后的角色信息
     */
    RoleDTO createRole(RoleDTO dto);

    /**
     * 更新角色
     *
     * @param roleId 角色ID
     * @param dto    角色信息
     */
    void updateRole(Long roleId, RoleDTO dto);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleDTO getRole(Long roleId);

    /**
     * 获取所有角色列表
     *
     * @return 角色列表
     */
    List<RoleDTO> listAllRoles();

    /**
     * 获取角色列表(根据状态)
     *
     * @param status 状态
     * @return 角色列表
     */
    List<RoleDTO> listRoles(Integer status);

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionDTO> getRolePermissions(Long roleId);

    /**
     * 更新角色权限
     *
     * @param roleId  角色ID
     * @param permIds 权限ID列表
     */
    void updateRolePermissions(Long roleId, List<Long> permIds);

    /**
     * 获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleDTO> getUserRoles(Long userId);

}
