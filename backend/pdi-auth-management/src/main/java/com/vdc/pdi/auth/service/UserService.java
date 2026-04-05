package com.vdc.pdi.auth.service;

import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.dto.request.CreateUserRequest;
import com.vdc.pdi.auth.dto.request.UpdateUserRequest;
import com.vdc.pdi.auth.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据ID查询用户
     */
    UserResponse getUserById(Long id);

    /**
     * 根据用户名查询用户
     */
    UserResponse getUserByUsername(String username);

    /**
     * 分页查询用户
     */
    Page<UserResponse> getUsers(Pageable pageable);

    /**
     * 根据部门ID查询用户
     */
    Page<UserResponse> getUsersByDeptId(Long deptId, Pageable pageable);

    /**
     * 创建用户
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * 更新用户
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 批量删除用户
     */
    void batchDeleteUsers(List<Long> ids);

    /**
     * 分配用户角色
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
