package com.vdc.pdi.auth.service.impl;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.domain.repository.RoleRepository;
import com.vdc.pdi.auth.domain.repository.UserRoleRepository;
import com.vdc.pdi.auth.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleByCode(String roleCode) {
        return roleRepository.findByRoleCodeAndNotDeleted(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found with code: " + roleCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Role> getRoles(Pageable pageable) {
        return roleRepository.findByDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllActiveRoles() {
        return roleRepository.findByStatusAndDeletedFalse(1);
    }

    @Override
    @Transactional
    public Role createRole(Role role) {
        // 检查角色编码是否已存在
        if (roleRepository.existsByRoleCode(role.getRoleCode())) {
            throw new RuntimeException("Role code already exists");
        }

        role.setStatus(1);
        role.setDeleted(false);

        Role savedRole = roleRepository.save(role);
        logger.info("Role created: {}", savedRole.getRoleCode());
        return savedRole;
    }

    @Override
    @Transactional
    public Role updateRole(Long id, Role role) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // 检查角色编码是否被其他角色使用
        if (!existingRole.getRoleCode().equals(role.getRoleCode())) {
            if (roleRepository.existsByRoleCode(role.getRoleCode())) {
                throw new RuntimeException("Role code already exists");
            }
        }

        existingRole.setRoleName(role.getRoleName());
        existingRole.setRoleCode(role.getRoleCode());
        existingRole.setDescription(role.getDescription());
        existingRole.setSortOrder(role.getSortOrder());
        existingRole.setDataScope(role.getDataScope());

        Role updatedRole = roleRepository.save(existingRole);
        logger.info("Role updated: {}", updatedRole.getRoleCode());
        return updatedRole;
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // 删除角色与用户的关联
        userRoleRepository.deleteByRoleId(id);

        // 逻辑删除角色
        role.setDeleted(true);
        roleRepository.save(role);

        logger.info("Role deleted: {}", role.getRoleCode());
    }

    @Override
    @Transactional
    public void batchDeleteRoles(List<Long> ids) {
        for (Long id : ids) {
            deleteRole(id);
        }
    }

    @Override
    @Transactional
    public void updateRoleStatus(Long id, Integer status) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        role.setStatus(status);
        roleRepository.save(role);
        logger.info("Role {} status updated to: {}", role.getRoleCode(), status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoleCode(String roleCode) {
        return roleRepository.existsByRoleCode(roleCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getRolesByUserId(Long userId) {
        return userRoleRepository.findRolesByUserId(userId);
    }
}
