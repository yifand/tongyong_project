package com.vdc.platform.controller;

import com.vdc.platform.common.ApiResult;
import com.vdc.platform.dto.LoginRequest;
import com.vdc.platform.dto.LoginResponse;
import com.vdc.platform.dto.RefreshTokenRequest;
import com.vdc.platform.entity.OperationLog;
import com.vdc.platform.security.jwt.JwtUtil;
import com.vdc.platform.security.jwt.TokenService;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.security.service.CustomUserDetailsService;
import com.vdc.platform.service.IOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final IOperationLogService operationLogService;

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            SecurityUser user = (SecurityUser) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            tokenService.storeRefreshToken(user.getUsername(), refreshToken);
            customUserDetailsService.clearLoginFailure(user.getUsername());

            recordOperationLog(user, "LOGIN", "User logged in", 1, httpRequest);

            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setRealName(null);
            response.setRoleCode(user.getRoleCode());
            response.setSiteId(user.getSiteId());
            response.setPermissions(user.getPermissions());

            return ApiResult.success(response);
        } catch (BadCredentialsException e) {
            customUserDetailsService.recordLoginFailure(request.getUsername());
            return ApiResult.error(401, "Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {
        SecurityUser user = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenService.blacklistAccessToken(accessToken);
            String username = jwtUtil.getUsernameFromToken(accessToken);
            try {
                user = (SecurityUser) customUserDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                // ignore if user not found
            }
            // Attempt to delete any associated refresh token from request body isn't available here,
            // client should discard refresh token. Blacklisting access token is sufficient.
        }
        if (user == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
                user = (SecurityUser) authentication.getPrincipal();
            }
        }
        if (user != null) {
            recordOperationLog(user, "LOGOUT", "User logged out", 1, httpRequest);
        }
        SecurityContextHolder.clearContext();
        return ApiResult.success();
    }

    @PostMapping("/refresh")
    public ApiResult<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            return ApiResult.error(401, "Invalid refresh token");
        }
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        if (!tokenService.validateRefreshToken(username, refreshToken)) {
            return ApiResult.error(401, "Refresh token expired or revoked");
        }

        SecurityUser user = (SecurityUser) customUserDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtUtil.generateAccessToken(user);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRoleCode(user.getRoleCode());
        response.setSiteId(user.getSiteId());
        response.setPermissions(user.getPermissions());

        return ApiResult.success(response);
    }

    private void recordOperationLog(SecurityUser user, String type, String content, int result, HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setUserId(user.getUserId());
        log.setUsername(user.getUsername());
        log.setIpAddress(getClientIp(request));
        log.setOperationType(type);
        log.setOperationContent(content);
        log.setResult(result);
        log.setCreatedAt(LocalDateTime.now());
        operationLogService.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
