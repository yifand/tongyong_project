package com.vdc.pdi.auth.service.impl;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.entity.UserRole;
import com.vdc.pdi.auth.domain.repository.RoleRepository;
import com.vdc.pdi.auth.domain.repository.UserRepository;
import com.vdc.pdi.auth.domain.repository.UserRoleRepository;
import com.vdc.pdi.auth.dto.request.CreateUserRequest;
import com.vdc.pdi.auth.dto.request.UpdateUserRequest;
import com.vdc.pdi.auth.dto.response.UserResponse;
import com.vdc.pdi.auth.service.PasswordValidator;
import com.vdc.pdi.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordValidator passwordValidator;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameAndNotDeleted(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByDeptId(Long deptId, Pageable pageable) {
        return userRepository.findByDeptIdAndDeletedFalse(deptId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 验证密码
        passwordValidator.validate(request.getPassword());

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setDeleted(false);

        User savedUser = userRepository.save(user);

        // 分配角色
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            assignRoles(savedUser.getId(), request.getRoleIds());
        }

        logger.info("User created: {}", savedUser.getUsername());
        return convertToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
        }

        // 更新用户信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getDeptId() != null) {
            user.setDeptId(request.getDeptId());
        }
        if (request.getDataScope() != null) {
            user.setDataScope(request.getDataScope());
        }

        User updatedUser = userRepository.save(user);

        // 更新角色
        if (request.getRoleIds() != null) {
            assignRoles(id, request.getRoleIds());
        }

        logger.info("User updated: {}", updatedUser.getUsername());
        return convertToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setDeleted(true);
        userRepository.save(user);
        logger.info("User deleted: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void batchDeleteUsers(List<Long> ids) {
        for (Long id : ids) {
            deleteUser(id);
        }
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色关联
        userRoleRepository.deleteByUserId(userId);

        // 添加新角色关联
        if (!CollectionUtils.isEmpty(roleIds)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                userRoleRepository.save(userRole);
            }
        }

        logger.info("Roles assigned to user {}: {}", userId, roleIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .map(ur -> ur.getRole().getId())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setStatus(status);
        userRepository.save(user);
        logger.info("User {} status updated to: {}", user.getUsername(), status);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        // 验证密码
        passwordValidator.validate(newPassword);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password reset for user: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 转换为响应对象
     */
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);

        // 获取用户角色
        List<Role> roles = userRoleRepository.findRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
        List<String> roleNames = roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        response.setRoleCodes(roleCodes);
        response.setRoleNames(roleNames);

        return response;
    }
}
