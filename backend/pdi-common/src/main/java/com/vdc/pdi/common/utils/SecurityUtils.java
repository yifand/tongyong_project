
package com.vdc.pdi.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 * 提供获取当前登录用户信息等功能
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 私有构造，防止实例化
    }

    /**
     * 获取当前认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户ID
     * 返回null表示未登录
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // 尝试从username解析为Long
            String username = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    /**
     * 判断是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 判断当前用户是否为匿名用户
     */
    public static boolean isAnonymous() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return true;
        }
        return "anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否拥有指定权限
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    /**
     * 检查当前用户是否拥有任意一个指定权限
     */
    public static boolean hasAnyAuthority(String... authorities) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (String authority : authorities) {
            if (hasAuthority(authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清除当前安全上下文
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
