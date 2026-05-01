package com.vdc.pdi.logmanagement.annotation;

import com.vdc.pdi.logmanagement.enums.OperationType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作类型
     */
    OperationType type();

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 参数过滤字段（不记录敏感字段）
     */
    String[] excludeParams() default {"password", "token", "secret"};
}
