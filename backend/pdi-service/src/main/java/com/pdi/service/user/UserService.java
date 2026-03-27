package com.pdi.service.user;

import com.pdi.common.result.PageResult;
import com.pdi.service.user.dto.PasswordChangeDTO;
import com.pdi.service.user.dto.UserDTO;
import com.pdi.service.user.dto.UserQueryDTO;

/**
 * 用户服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param dto 用户信息
     * @return 创建后的用户信息
     */
    UserDTO createUser(UserDTO dto);

    /**
     * 更新用户
     *
     * @param userId 用户ID
     * @param dto    用户信息
     */
    void updateUser(Long userId, UserDTO dto);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserDTO getUser(Long userId);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserDTO getUserByUsername(String username);

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<UserDTO> listUsers(UserQueryDTO query);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 状态: 0-禁用, 1-启用
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 重置密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param dto    密码修改信息
     */
    void changePassword(Long userId, PasswordChangeDTO dto);

    /**
     * 更新用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void updateUserRoles(Long userId, java.util.List<Long> roleIds);

    /**
     * 记录登录日志
     *
     * @param userId  用户ID
     * @param ip      IP地址
     * @param success 是否成功
     */
    void recordLoginLog(Long userId, String ip, boolean success);

}
