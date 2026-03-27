package com.pdi.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdi.common.enums.StatusEnum;
import com.pdi.common.exception.BusinessException;
import com.pdi.common.result.PageResult;
import com.pdi.common.result.ResultCode;
import com.pdi.common.utils.SecurityUtils;
import com.pdi.dao.entity.SysRole;
import com.pdi.dao.entity.SysUser;
import com.pdi.dao.entity.SysUserRole;
import com.pdi.dao.mapper.SysRoleMapper;
import com.pdi.dao.mapper.SysUserMapper;
import com.pdi.dao.mapper.SysUserRoleMapper;
import com.pdi.service.user.dto.PasswordChangeDTO;
import com.pdi.service.user.dto.UserDTO;
import com.pdi.service.user.dto.UserQueryDTO;
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
 * 用户服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(UserDTO dto) {
        // 检查用户名唯一性
        if (lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).exists()) {
            throw new BusinessException(5002, "用户名已存在");
        }

        // 检查手机号唯一性
        if (StringUtils.hasText(dto.getPhone()) &&
                lambdaQuery().eq(SysUser::getPhone, dto.getPhone()).exists()) {
            throw new BusinessException(5003, "手机号已存在");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);

        // 设置默认密码
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(SecurityUtils.encryptPassword("123456"));
        } else {
            user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        }

        user.setStatus(StatusEnum.ENABLED.getCode());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        save(user);

        // 保存用户角色关系
        if (!CollectionUtils.isEmpty(dto.getRoleIds())) {
            saveUserRoles(user.getId(), dto.getRoleIds());
        }

        log.info("创建用户: userId={}, username={}", user.getId(), user.getUsername());

        return convertToDTO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserDTO dto) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        // 检查用户名唯一性
        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(user.getUsername())) {
            if (lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).exists()) {
                throw new BusinessException(5002, "用户名已存在");
            }
        }

        // 检查手机号唯一性
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            if (lambdaQuery().eq(SysUser::getPhone, dto.getPhone()).exists()) {
                throw new BusinessException(5003, "手机号已存在");
            }
        }

        BeanUtils.copyProperties(dto, user, "id", "password", "createdAt", "createdBy");
        user.setId(userId);
        user.setUpdatedAt(LocalDateTime.now());

        updateById(user);

        // 更新用户角色关系
        if (dto.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId));
            if (!dto.getRoleIds().isEmpty()) {
                saveUserRoles(userId, dto.getRoleIds());
            }
        }

        log.info("更新用户: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        // 不能删除超级管理员
        if (isSuperAdmin(userId)) {
            throw new BusinessException(5006, "不能删除超级管理员");
        }

        // 删除用户角色关系
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        removeById(userId);

        log.info("删除用户: userId={}", userId);
    }

    @Override
    public UserDTO getUser(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).one();
        return user != null ? convertToDTO(user) : null;
    }

    @Override
    public PageResult<UserDTO> listUsers(UserQueryDTO query) {
        Page<SysUser> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (query.getSiteId() != null) {
            wrapper.eq(SysUser::getSiteId, query.getSiteId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or()
                    .like(SysUser::getRealName, query.getKeyword()));
        }

        wrapper.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> pageResult = page(pageParam, wrapper);

        List<UserDTO> list = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        // 不能禁用超级管理员
        if (status == StatusEnum.DISABLED.getCode() && isSuperAdmin(userId)) {
            throw new BusinessException(5006, "不能禁用超级管理员");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);

        log.info("更新用户状态: userId={}, status={}", userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);

        log.info("重置密码: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, PasswordChangeDTO dto) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        // 验证旧密码
        if (!SecurityUtils.matchesPassword(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(5007, "旧密码错误");
        }

        user.setPassword(SecurityUtils.encryptPassword(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);

        log.info("修改密码: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(5001, "用户不存在");
        }

        // 删除原有角色关系
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        // 保存新的角色关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            saveUserRoles(userId, roleIds);
        }

        log.info("更新用户角色: userId={}, roleIds={}", userId, roleIds);
    }

    @Override
    public void recordLoginLog(Long userId, String ip, boolean success) {
        SysUser user = getById(userId);
        if (user == null) {
            return;
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        updateById(user);
    }

    // ==================== 私有方法 ====================

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        List<SysUserRole> userRoles = roleIds.stream().map(roleId -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).collect(Collectors.toList());

        for (SysUserRole userRole : userRoles) {
            userRoleMapper.insert(userRole);
        }
    }

    private UserDTO convertToDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setPassword(null); // 不返回密码

        // 设置状态名称
        StatusEnum statusEnum = StatusEnum.getByCode(user.getStatus());
        if (statusEnum != null) {
            dto.setStatusName(statusEnum.getDescription());
        }

        // TODO: 查询站点名称
        if (user.getSiteId() != null) {
            dto.setSiteName("未知站点");
        }

        // 查询用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId()));

        if (!CollectionUtils.isEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            dto.setRoleIds(roleIds);

            // 查询角色详情
            List<UserDTO.UserRoleVO> roles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysRole role = roleMapper.selectById(roleId);
                if (role != null) {
                    UserDTO.UserRoleVO roleVO = new UserDTO.UserRoleVO();
                    roleVO.setId(role.getId());
                    roleVO.setRoleCode(role.getRoleCode());
                    roleVO.setRoleName(role.getRoleName());
                    roles.add(roleVO);
                }
            }
            dto.setRoles(roles);
        }

        return dto;
    }

    private boolean isSuperAdmin(Long userId) {
        // 假设ID为1的用户是超级管理员
        return userId != null && userId == 1L;
    }

}
