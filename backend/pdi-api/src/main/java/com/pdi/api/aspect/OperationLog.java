package com.pdi.api.aspect;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operation() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveParams() default true;

    /**
     * 是否保存响应结果
     */
    boolean saveResult() default false;
}
