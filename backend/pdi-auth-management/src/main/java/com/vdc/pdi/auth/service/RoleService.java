package com.vdc.pdi.auth.service;

import com.vdc.pdi.auth.domain.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 根据ID查询角色
     */
    Role getRoleById(Long id);

    /**
     * 根据角色编码查询角色
     */
    Role getRoleByCode(String roleCode);

    /**
     * 分页查询角色
     */
    Page<Role> getRoles(Pageable pageable);

    /**
     * 查询所有有效角色
     */
    List<Role> getAllActiveRoles();

    /**
     * 创建角色
     */
    Role createRole(Role role);

    /**
     * 更新角色
     */
    Role updateRole(Long id, Role role);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 批量删除角色
     */
    void batchDeleteRoles(List<Long> ids);

    /**
     * 更新角色状态
     */
    void updateRoleStatus(Long id, Integer status);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> getRolesByUserId(Long userId);
}
