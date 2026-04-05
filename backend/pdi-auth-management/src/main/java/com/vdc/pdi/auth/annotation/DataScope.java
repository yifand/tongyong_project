package com.vdc.pdi.auth.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于标记需要数据权限控制的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    
    /**
     * 部门表的别名
     */
    String deptAlias() default "";
    
    /**
     * 用户表的别名
     */
    String userAlias() default "";
    
    /**
     * 是否启用数据权限
     */
    boolean enabled() default true;
}
