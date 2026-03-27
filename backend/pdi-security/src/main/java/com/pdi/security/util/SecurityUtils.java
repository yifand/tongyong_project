package com.pdi.security.util;

import com.pdi.security.service.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;

/**
 * 安全工具类
 * <p>
 * 提供与安全相关的工具方法，包括获取当前用户、密码加密等
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
public class SecurityUtils {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private SecurityUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前登录用户
     *
     * @return SecurityUser对象，未登录返回null
     */
    public static SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof SecurityUser) {
            return (SecurityUser) principal;
        }
        
        // 匿名用户或字符串principal
        if ("anonymousUser".equals(principal)) {
            return null;
        }
        
        log.warn("无法识别的principal类型: {}", principal.getClass().getName());
        return null;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        SecurityUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，未登录返回null
     */
    public static String getCurrentUsername() {
        SecurityUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户站点ID
     *
     * @return 站点ID，未登录或无站点返回null
     */
    public static Long getCurrentSiteId() {
        SecurityUser user = getCurrentUser();
        return user != null ? user.getSiteId() : null;
    }

    /**
     * 判断当前用户是否为超级管理员
     *
     * @return 是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        SecurityUser user = getCurrentUser();
        return user != null && user.isSuperAdmin();
    }

    /**
     * 检查当前用户是否有指定权限
     *
     * @param permission 权限编码
     * @return 是否有权限
     */
    public static boolean hasPermission(String permission) {
        SecurityUser user = getCurrentUser();
        return user != null && user.hasPermission(permission);
    }

    /**
     * 检查当前用户是否有指定角色
     *
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    public static boolean hasRole(String roleCode) {
        SecurityUser user = getCurrentUser();
        return user != null && user.hasRole(roleCode);
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null 
                && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * BCrypt加密密码
     *
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return passwordEncoder.encode(password);
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 获取当前用户的所有权限
     *
     * @return 权限集合
     */
    public static Collection<?> getCurrentUserAuthorities() {
        SecurityUser user = getCurrentUser();
        return user != null ? user.getAuthorities() : null;
    }

    /**
     * 强制获取当前用户（未登录抛出异常）
     *
     * @return SecurityUser对象
     * @throws IllegalStateException 未登录时抛出
     */
    public static SecurityUser requireCurrentUser() {
        SecurityUser user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        return user;
    }

    /**
     * 生成随机密码
     *
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        
        return sb.toString();
    }
}
