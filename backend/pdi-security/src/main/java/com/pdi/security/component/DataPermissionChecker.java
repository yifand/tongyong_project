package com.pdi.security.component;

import com.pdi.security.service.SecurityUser;
import com.pdi.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据权限检查器
 * <p>
 * 用于检查当前用户是否有权限访问指定数据。
 * 超级管理员拥有所有数据权限，普通用户只能访问本站点数据。
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component("dataPermissionChecker")
public class DataPermissionChecker {

    /**
     * 检查数据权限
     *
     * @param joinPoint 切入点
     * @return 是否有权限
     */
    public boolean check(JoinPoint joinPoint) {
        SecurityUser currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            log.warn("数据权限检查失败：当前用户未登录");
            return false;
        }

        // 超级管理员拥有所有数据权限
        if (currentUser.isSuperAdmin()) {
            log.debug("数据权限检查通过：超级管理员拥有所有权限");
            return true;
        }

        // 从参数中获取站点ID
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            
            // 检查是否有getSiteId方法
            Long siteId = extractSiteId(arg);
            if (siteId != null) {
                // 检查用户站点权限
                boolean hasPermission = checkSitePermission(currentUser, siteId);
                if (!hasPermission) {
                    log.warn("数据权限检查失败：用户{}无权访问站点{}", 
                            currentUser.getUsername(), siteId);
                }
                return hasPermission;
            }
        }

        // 如果参数中没有siteId，默认允许访问（后续通过SQL拦截器过滤）
        log.debug("数据权限检查：参数中未找到siteId，默认允许");
        return true;
    }

    /**
     * 检查简单数据权限（无JoinPoint版本）
     *
     * @param siteId 站点ID
     * @return 是否有权限
     */
    public boolean checkSitePermission(Long siteId) {
        SecurityUser currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return false;
        }

        if (currentUser.isSuperAdmin()) {
            return true;
        }

        return checkSitePermission(currentUser, siteId);
    }

    /**
     * 检查用户是否有指定站点的权限
     *
     * @param user 当前用户
     * @param siteId 站点ID
     * @return 是否有权限
     */
    private boolean checkSitePermission(SecurityUser user, Long siteId) {
        // 如果用户没有分配站点，则只能查看无站点数据
        if (user.getSiteId() == null) {
            return siteId == null;
        }
        
        return user.getSiteId().equals(siteId);
    }

    /**
     * 从对象中提取站点ID
     *
     * @param obj 对象
     * @return 站点ID，如果无法提取则返回null
     */
    private Long extractSiteId(Object obj) {
        try {
            // 如果是Long类型直接返回
            if (obj instanceof Long) {
                return (Long) obj;
            }
            
            // 如果是Number类型，转换为Long
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            }
            
            // 尝试通过反射调用getSiteId方法
            Method method = obj.getClass().getMethod("getSiteId");
            Object result = method.invoke(obj);
            if (result instanceof Long) {
                return (Long) result;
            }
            if (result instanceof Number) {
                return ((Number) result).longValue();
            }
        } catch (NoSuchMethodException e) {
            // 没有getSiteId方法，忽略
            log.trace("对象{}没有getSiteId方法", obj.getClass().getName());
        } catch (Exception e) {
            log.warn("提取siteId时发生错误: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * 获取当前用户的数据权限SQL条件
     *
     * @return SQL条件字符串，如果没有限制则返回null
     */
    public String getDataScopeCondition() {
        SecurityUser currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return "1=0"; // 无权限
        }

        if (currentUser.isSuperAdmin()) {
            return null; // 超级管理员无限制
        }

        if (currentUser.getSiteId() != null) {
            return "site_id = " + currentUser.getSiteId();
        }

        return "site_id IS NULL";
    }

    /**
     * 获取当前用户的站点ID
     *
     * @return 站点ID，超级管理员返回null
     */
    public Long getCurrentSiteId() {
        SecurityUser currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null || currentUser.isSuperAdmin()) {
            return null;
        }
        
        return currentUser.getSiteId();
    }
}
