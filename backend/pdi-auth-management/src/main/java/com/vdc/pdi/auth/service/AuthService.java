package com.vdc.pdi.auth.service;

import com.vdc.pdi.auth.dto.request.LoginRequest;
import com.vdc.pdi.auth.dto.response.LoginResponse;
import com.vdc.pdi.auth.dto.response.UserResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String ipAddress);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 获取当前登录用户信息
     */
    UserResponse getCurrentUser(String username);

    /**
     * 修改密码
     */
    void changePassword(String username, String oldPassword, String newPassword);
}
