package com.vdc.pdi.auth.security;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.repository.UserRepository;
import com.vdc.pdi.auth.domain.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查找用户
        User user = userRepository.findByUsernameAndNotDeleted(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        // 获取用户角色
        List<Role> roles = userRoleRepository.findRolesByUserId(user.getId());

        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
                .collect(Collectors.toList());

        // 返回Spring Security的User对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true,
                true,
                true,
                authorities
        );
    }

    /**
     * 加载用户详情（包含完整用户信息）
     */
    @Transactional(readOnly = true)
    public User loadUserEntityByUsername(String username) {
        return userRepository.findByUsernameAndNotDeleted(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
