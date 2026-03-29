
package com.vdc.pdi.common.utils;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationUtils 单元测试
 */
class ValidationUtilsTest {

    @Test
    void testValidate() {
        ValidTestObject validObj = new ValidTestObject("test", 25, "test@example.com");
        Map<String, String> errors = ValidationUtils.validate(validObj);
        assertTrue(errors.isEmpty());

        ValidTestObject invalidObj = new ValidTestObject("", 5, "invalid-email");
        errors = ValidationUtils.validate(invalidObj);
        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("name") || errors.containsKey("age") || errors.containsKey("email"));
    }

    @Test
    void testValidateNull() {
        Map<String, String> errors = ValidationUtils.validate(null);
        assertTrue(errors.isEmpty());
    }

    @Test
    void testIsValid() {
        ValidTestObject validObj = new ValidTestObject("test", 25, "test@example.com");
        assertTrue(ValidationUtils.isValid(validObj));

        ValidTestObject invalidObj = new ValidTestObject("", 5, "invalid");
        assertFalse(ValidationUtils.isValid(invalidObj));

        assertFalse(ValidationUtils.isValid(null));
    }

    @Test
    void testValidateFirst() {
        ValidTestObject invalidObj = new ValidTestObject("", 5, "invalid");
        String error = ValidationUtils.validateFirst(invalidObj);
        assertNotNull(error);
        assertFalse(error.isEmpty());

        ValidTestObject validObj = new ValidTestObject("test", 25, "test@example.com");
        assertNull(ValidationUtils.validateFirst(validObj));

        assertEquals("对象不能为空", ValidationUtils.validateFirst(null));
    }

    @Test
    void testValidateField() {
        ValidTestObject obj = new ValidTestObject("test", 5, "test@example.com");

        String nameError = ValidationUtils.validateField(obj, "name");
        assertNull(nameError);

        String ageError = ValidationUtils.validateField(obj, "age");
        assertNotNull(ageError);

        assertNull(ValidationUtils.validateField(null, "name"));
        assertNull(ValidationUtils.validateField(obj, null));
    }

    @Test
    void testValidateValue() {
        String error = ValidationUtils.validateValue(ValidTestObject.class, "name", "");
        assertNotNull(error);

        error = ValidationUtils.validateValue(ValidTestObject.class, "name", "valid");
        assertNull(error);

        assertNull(ValidationUtils.validateValue(null, "name", "value"));
        assertNull(ValidationUtils.validateValue(ValidTestObject.class, null, "value"));
    }

    @Test
    void testValidateAndThrow() {
        ValidTestObject validObj = new ValidTestObject("test", 25, "test@example.com");
        assertDoesNotThrow(() -> ValidationUtils.validateAndThrow(validObj));

        ValidTestObject invalidObj = new ValidTestObject("", 5, "invalid");
        assertThrows(ValidationException.class, () -> ValidationUtils.validateAndThrow(invalidObj));

        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateAndThrow(null));
    }

    @Test
    void testGetValidator() {
        assertNotNull(ValidationUtils.getValidator());
    }

    /**
     * 用于测试的验证对象
     */
    static class ValidTestObject {
        @NotBlank(message = "名称不能为空")
        private String name;

        @Min(value = 18, message = "年龄必须大于等于18")
        private int age;

        @Email(message = "邮箱格式不正确")
        private String email;

        ValidTestObject(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }
    }
}
