package com.pdi.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 用于控制数据级别的访问权限。
 * 超级管理员可以访问所有数据，普通用户只能访问本站点数据。
 * <p>
 * 使用示例：
 * <pre>
 *   &#64;DataPermission
 *   &#64;GetMapping("/alarms")
 *   public Result&lt;PageResult&lt;AlarmVO&gt;&gt; list(AlarmQueryDTO query) {
 *       // 自动进行数据权限过滤
 *       return Result.success(alarmService.list(query));
 *   }
 * </pre>
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('ADMIN') or @dataPermissionChecker.check(#root)")
public @interface DataPermission {

    /**
     * 数据权限类型
     */
    DataPermissionType type() default DataPermissionType.SITE;

    /**
     * 数据权限字段名
     */
    String field() default "siteId";

    /**
     * 数据权限类型枚举
     */
    enum DataPermissionType {
        /**
         * 站点级别权限
         */
        SITE,
        
        /**
         * 部门级别权限
         */
        DEPT,
        
        /**
         * 个人级别权限
         */
        PERSONAL,
        
        /**
         * 自定义
         */
        CUSTOM
    }
}
