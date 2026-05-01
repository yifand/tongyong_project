package com.vdc.platform.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdc.platform.entity.SysRole;
import com.vdc.platform.entity.SysUser;
import com.vdc.platform.mapper.SysRoleMapper;
import com.vdc.platform.mapper.SysUserMapper;
import com.vdc.platform.security.model.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final int MAX_LOGIN_FAIL = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String lockKey = "login_fail:" + username;
        String lockValue = stringRedisTemplate.opsForValue().get(lockKey);
        if (lockValue != null) {
            try {
                int failCount = Integer.parseInt(lockValue);
                if (failCount >= MAX_LOGIN_FAIL) {
                    throw new org.springframework.security.authentication.LockedException("Account locked due to too many failed login attempts. Try again in 5 minutes.");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        List<String> permissions = role != null && role.getPermissions() != null ? role.getPermissions() : List.of();

        SecurityUser securityUser = new SecurityUser();
        securityUser.setUserId(user.getId());
        securityUser.setUsername(user.getUsername());
        securityUser.setPassword(user.getPasswordHash());
        securityUser.setSiteId(user.getSiteId());
        securityUser.setRoleCode(role != null ? role.getRoleCode() : null);
        securityUser.setDataScope(role != null ? role.getDataScope() : null);
        securityUser.setPermissions(permissions);
        securityUser.setEnabled(user.getStatus() != null && user.getStatus() == 1);
        securityUser.setAccountNonExpired(true);
        securityUser.setAccountNonLocked(true);
        securityUser.setCredentialsNonExpired(true);

        return securityUser;
    }

    public void recordLoginFailure(String username) {
        String key = "login_fail:" + username;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, LOCK_DURATION);
        }
    }

    public void clearLoginFailure(String username) {
        stringRedisTemplate.delete("login_fail:" + username);
    }
}
