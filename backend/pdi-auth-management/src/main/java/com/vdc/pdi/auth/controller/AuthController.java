package com.vdc.pdi.auth.controller;

import com.vdc.pdi.auth.dto.request.LoginRequest;
import com.vdc.pdi.auth.dto.response.LoginResponse;
import com.vdc.pdi.auth.dto.response.UserResponse;
import com.vdc.pdi.auth.service.AuthService;
import com.vdc.pdi.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        LoginResponse response = authService.login(request, ipAddress);
        return ApiResponse.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        authService.logout(token);
        return ApiResponse.success(null);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public ApiResponse<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserResponse response = authService.getCurrentUser(username);
        return ApiResponse.success(response);
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
