package com.vdc.pdi.auth.service.impl;

import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.repository.UserRepository;
import com.vdc.pdi.auth.dto.request.LoginRequest;
import com.vdc.pdi.auth.dto.response.LoginResponse;
import com.vdc.pdi.auth.dto.response.UserResponse;
import com.vdc.pdi.auth.security.JwtTokenProvider;
import com.vdc.pdi.auth.security.TokenBlacklistService;
import com.vdc.pdi.auth.service.AuthService;
import com.vdc.pdi.auth.service.LoginLockService;
import com.vdc.pdi.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private LoginLockService loginLockService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        String username = request.getUsername();

        // 检查账户是否被锁定
        if (loginLockService.isLocked(username)) {
            long remainingTime = loginLockService.getRemainingLockTime(username);
            throw new RuntimeException("Account is locked. Please try again after " + remainingTime + " minutes");
        }

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // 更新登录信息
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            userRepository.updateLastLoginInfo(user.getId(), LocalDateTime.now(), ipAddress);

            // 清除登录失败记录
            loginLockService.unlock(username);

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenProvider.getTokenRemainingTime(accessToken) / 1000);

            // 获取用户信息
            UserResponse userResponse = userService.getUserByUsername(username);
            response.setUser(userResponse);

            logger.info("User {} logged in successfully from IP: {}", username, ipAddress);
            return response;

        } catch (BadCredentialsException e) {
            // 记录登录失败
            loginLockService.recordFailedAttempt(username);
            int failCount = loginLockService.getFailedAttempts(username);
            logger.warn("Failed login attempt {} for user {} from IP: {}", failCount, username, ipAddress);
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 将Token加入黑名单
            long expirationTime = jwtTokenProvider.getTokenRemainingTime(token);
            tokenBlacklistService.blacklistToken(token, expirationTime);
            SecurityContextHolder.clearContext();
            logger.info("User logged out successfully");
        }
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 检查Token是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        String username = jwtTokenProvider.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 生成新的Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        String newAccessToken = jwtTokenProvider.generateToken(userDetails, claims);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // 将旧刷新Token加入黑名单
        tokenBlacklistService.blacklistToken(refreshToken, jwtTokenProvider.getTokenRemainingTime(refreshToken));

        LoginResponse response = new LoginResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getTokenRemainingTime(newAccessToken) / 1000);

        // 获取用户信息
        UserResponse userResponse = userService.getUserByUsername(username);
        response.setUser(userResponse);

        logger.info("Token refreshed for user: {}", username);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        return userService.getUserByUsername(username);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed for user: {}", username);
    }
}
