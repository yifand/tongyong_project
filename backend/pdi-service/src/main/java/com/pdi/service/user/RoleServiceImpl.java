package com.pdi.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdi.common.enums.DataScopeEnum;
import com.pdi.common.enums.StatusEnum;
import com.pdi.common.exception.BusinessException;
import com.pdi.dao.entity.SysPermission;
import com.pdi.dao.entity.SysRole;
import com.pdi.dao.entity.SysRolePermission;
import com.pdi.dao.entity.SysUserRole;
import com.pdi.dao.mapper.SysPermissionMapper;
import com.pdi.dao.mapper.SysRoleMapper;
import com.pdi.dao.mapper.SysRolePermissionMapper;
import com.pdi.dao.mapper.SysUserRoleMapper;
import com.pdi.service.user.dto.PermissionDTO;
import com.pdi.service.user.dto.RoleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO createRole(RoleDTO dto) {
        // 检查角色编码唯一性
        if (lambdaQuery().eq(SysRole::getRoleCode, dto.getRoleCode()).exists()) {
            throw new BusinessException(5005, "角色编码已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setStatus(StatusEnum.ENABLED.getCode());
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());

        save(role);

        // 保存角色权限关系
        if (!CollectionUtils.isEmpty(dto.getPermissionIds())) {
            saveRolePermissions(role.getId(), dto.getPermissionIds());
        }

        log.info("创建角色: roleId={}, roleCode={}", role.getId(), role.getRoleCode());

        return convertToDTO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleDTO dto) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(5004, "角色不存在");
        }

        // 检查角色编码唯一性
        if (StringUtils.hasText(dto.getRoleCode()) && !dto.getRoleCode().equals(role.getRoleCode())) {
            if (lambdaQuery().eq(SysRole::getRoleCode, dto.getRoleCode()).exists()) {
                throw new BusinessException(5005, "角色编码已存在");
            }
        }

        BeanUtils.copyProperties(dto, role, "id", "createdAt", "createdBy");
        role.setId(roleId);
        role.setUpdatedAt(LocalDateTime.now());

        updateById(role);

        // 更新角色权限关系
        if (dto.getPermissionIds() != null) {
            rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                    .eq(SysRolePermission::getRoleId, roleId));
            if (!dto.getPermissionIds().isEmpty()) {
                saveRolePermissions(roleId, dto.getPermissionIds());
            }
        }

        log.info("更新角色: roleId={}", roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(5004, "角色不存在");
        }

        // 检查是否有用户使用该角色
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId));
        if (userCount > 0) {
            throw new BusinessException("该角色已被用户使用，不能删除");
        }

        // 删除角色权限关系
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));

        removeById(roleId);

        log.info("删除角色: roleId={}", roleId);
    }

    @Override
    public RoleDTO getRole(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(5004, "角色不存在");
        }

        return convertToDTO(role);
    }

    @Override
    public List<RoleDTO> listAllRoles() {
        List<SysRole> roles = lambdaQuery()
                .eq(SysRole::getStatus, StatusEnum.ENABLED.getCode())
                .orderByAsc(SysRole::getSortOrder)
                .list();

        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleDTO> listRoles(Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getSortOrder);

        return list(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionDTO> getRolePermissions(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(5004, "角色不存在");
        }

        // 获取角色已分配的权限ID
        List<Long> rolePermIds = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>()
                                .eq(SysRolePermission::getRoleId, roleId))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 获取所有权限
        List<SysPermission> allPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .orderByAsc(SysPermission::getSortOrder));

        // 构建权限树并标记已选中的权限
        return buildPermissionTree(allPermissions, rolePermIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<Long> permIds) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(5004, "角色不存在");
        }

        // 删除原有权限关系
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));

        // 保存新的权限关系
        if (!CollectionUtils.isEmpty(permIds)) {
            saveRolePermissions(roleId, permIds);
        }

        log.info("更新角色权限: roleId={}, permIds={}", roleId, permIds);
    }

    @Override
    public List<RoleDTO> getUserRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }

        return listByIds(roleIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private void saveRolePermissions(Long roleId, List<Long> permIds) {
        List<SysRolePermission> rolePermissions = permIds.stream().map(permId -> {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permId);
            return rolePermission;
        }).collect(Collectors.toList());

        for (SysRolePermission rolePermission : rolePermissions) {
            rolePermissionMapper.insert(rolePermission);
        }
    }

    private RoleDTO convertToDTO(SysRole role) {
        RoleDTO dto = new RoleDTO();
        BeanUtils.copyProperties(role, dto);

        // 设置状态名称
        StatusEnum statusEnum = StatusEnum.getByCode(role.getStatus());
        if (statusEnum != null) {
            dto.setStatusName(statusEnum.getDescription());
        }

        // TODO: 设置数据范围名称 (需要添加 dataScope 字段到 SysRole 实体)
        dto.setDataScope(2); // 默认本站点数据
        dto.setDataScopeName("本站点数据");

        // 查询用户数量
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, role.getId()));
        dto.setUserCount(userCount.intValue());

        return dto;
    }

    private List<PermissionDTO> buildPermissionTree(List<SysPermission> permissions, List<Long> rolePermIds) {
        List<PermissionDTO> result = new ArrayList<>();

        // 先构建所有节点的DTO
        for (SysPermission perm : permissions) {
            PermissionDTO dto = convertPermissionToDTO(perm);
            dto.setChecked(rolePermIds.contains(perm.getId()));
            result.add(dto);
        }

        // 构建树形结构
        List<PermissionDTO> rootNodes = result.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .collect(Collectors.toList());

        for (PermissionDTO root : rootNodes) {
            buildChildren(root, result);
        }

        return rootNodes;
    }

    private void buildChildren(PermissionDTO parent, List<PermissionDTO> allPermissions) {
        List<PermissionDTO> children = allPermissions.stream()
                .filter(p -> parent.getId().equals(p.getParentId()))
                .collect(Collectors.toList());

        if (!children.isEmpty()) {
            parent.setChildren(children);
            for (PermissionDTO child : children) {
                buildChildren(child, allPermissions);
            }
        }
    }

    private PermissionDTO convertPermissionToDTO(SysPermission perm) {
        PermissionDTO dto = new PermissionDTO();
        BeanUtils.copyProperties(perm, dto);
        // TODO: 设置权限类型名称
        dto.setPermTypeName(getPermissionTypeName(perm.getPermissionType()));
        return dto;
    }

    private String getPermissionTypeName(String permType) {
        return switch (permType) {
            case "MENU" -> "菜单";
            case "BUTTON" -> "按钮";
            case "API" -> "接口";
            default -> "未知";
        };
    }

}
