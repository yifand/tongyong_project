
package com.vdc.pdi.common.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 校验工具类
 * 提供对象属性校验功能
 */
public final class ValidationUtils {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private ValidationUtils() {
        // 私有构造，防止实例化
    }

    /**
     * 获取Validator实例
     */
    public static Validator getValidator() {
        return VALIDATOR;
    }

    /**
     * 校验对象，返回所有错误信息
     *
     * @param obj 要校验的对象
     * @return 字段名与错误信息的映射，无错误返回空Map
     */
    public static Map<String, String> validate(Object obj) {
        Map<String, String> errors = new HashMap<>();
        if (obj == null) {
            return errors;
        }

        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(obj);
        for (ConstraintViolation<Object> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(fieldName, message);
        }
        return errors;
    }

    /**
     * 校验对象，返回是否通过
     *
     * @param obj 要校验的对象
     * @return 是否通过校验
     */
    public static boolean isValid(Object obj) {
        if (obj == null) {
            return false;
        }
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(obj);
        return violations.isEmpty();
    }

    /**
     * 校验对象，失败时返回第一条错误信息
     *
     * @param obj 要校验的对象
     * @return 第一条错误信息，无错误返回null
     */
    public static String validateFirst(Object obj) {
        if (obj == null) {
            return "对象不能为空";
        }

        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(obj);
        if (!violations.isEmpty()) {
            return violations.iterator().next().getMessage();
        }
        return null;
    }

    /**
     * 校验指定字段
     *
     * @param obj       要校验的对象
     * @param fieldName 字段名
     * @return 错误信息，无错误返回null
     */
    public static String validateField(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }

        Set<ConstraintViolation<Object>> violations = VALIDATOR.validateProperty(obj, fieldName);
        if (!violations.isEmpty()) {
            return violations.iterator().next().getMessage();
        }
        return null;
    }

    /**
     * 校验指定字段的值
     *
     * @param beanType  Bean类型
     * @param fieldName 字段名
     * @param value     字段值
     * @return 错误信息，无错误返回null
     */
    public static <T> String validateValue(Class<T> beanType, String fieldName, Object value) {
        if (beanType == null || fieldName == null) {
            return null;
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validateValue(beanType, fieldName, value);
        if (!violations.isEmpty()) {
            return violations.iterator().next().getMessage();
        }
        return null;
    }

    /**
     * 校验并抛出异常（如果需要）
     *
     * @param obj 要校验的对象
     * @throws jakarta.validation.ConstraintViolationException 校验失败时抛出
     */
    public static void validateAndThrow(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("校验对象不能为空");
        }

        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(obj);
        if (!violations.isEmpty()) {
            throw new jakarta.validation.ConstraintViolationException(violations);
        }
    }
}
