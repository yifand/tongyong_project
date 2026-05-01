package com.vdc.platform.security.interceptor;

import java.lang.reflect.Field;

public class ReflectUtil {

    public static void setFieldValue(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Reflect set field failed: " + fieldName, e);
        }
    }
}
