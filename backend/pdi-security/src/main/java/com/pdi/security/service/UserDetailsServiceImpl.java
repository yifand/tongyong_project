package com.pdi.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdi.dao.entity.SysPermission;
import com.pdi.dao.entity.SysRole;
import com.pdi.dao.entity.SysUser;
import com.pdi.dao.entity.SysUserRole;
import com.pdi.dao.mapper.SysPermissionMapper;
import com.pdi.dao.mapper.SysRoleMapper;
import com.pdi.dao.mapper.SysUserMapper;
import com.pdi.dao.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现类
 * <p>
 * 实现Spring Security的UserDetailsService接口，从数据库加载用户信息
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysPermissionMapper permissionMapper;

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名或用户ID字符串
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 尝试将username解析为用户ID（JWT中存储的是userId）
        Long userId = null;
        try {
            userId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            // 不是数字，按用户名查询
        }

        SysUser user;
        if (userId != null) {
            user = userMapper.selectById(userId);
        } else {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, username);
            user = userMapper.selectOne(wrapper);
        }

        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return buildSecurityUser(user);
    }

    /**
     * 根据用户ID加载用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: ID={}", userId);
            throw new UsernameNotFoundException("用户不存在: ID=" + userId);
        }
        return buildSecurityUser(user);
    }

    /**
     * 构建SecurityUser对象
     *
     * @param user 系统用户实体
     * @return SecurityUser对象
     */
    private SecurityUser buildSecurityUser(SysUser user) {
        // 查询用户角色
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(SysUserRole::getUserId, user.getId());
        List<SysUserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);

        String roleCode = null;
        Long siteId = null;
        Set<String> permissions = new HashSet<>();

        if (!userRoles.isEmpty()) {
            // 获取第一个角色（简化处理，假设用户只有一个角色）
            Long roleId = userRoles.get(0).getRoleId();
            SysRole role = roleMapper.selectById(roleId);
            if (role != null) {
                roleCode = role.getRoleCode();
            }

            // 获取角色权限
            List<SysPermission> permissionList = permissionMapper.selectByRoleId(roleId);
            permissions = permissionList.stream()
                    .map(SysPermission::getPermissionCode)
                    .collect(Collectors.toSet());
        }

        // 构建SecurityUser
        SecurityUser securityUser = new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                siteId,
                roleCode,
                user.getStatus()
        );
        securityUser.setPermissions(permissions);

        log.debug("加载用户详情成功: {}, 角色: {}, 权限数: {}", 
                user.getUsername(), roleCode, permissions.size());

        return securityUser;
    }
}
