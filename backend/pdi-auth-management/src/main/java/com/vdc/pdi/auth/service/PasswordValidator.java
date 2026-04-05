package com.vdc.pdi.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 密码策略验证器
 */
@Component
public class PasswordValidator {

    @Value("${auth.password.min-length:8}")
    private int minLength;

    @Value("${auth.password.max-length:32}")
    private int maxLength;

    @Value("${auth.password.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${auth.password.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${auth.password.require-digit:true}")
    private boolean requireDigit;

    @Value("${auth.password.require-special:true}")
    private boolean requireSpecial;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-\\[\\]{};':\"|,.<>/?]");

    /**
     * 验证密码
     */
    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // 检查长度
        if (password.length() < minLength) {
            throw new IllegalArgumentException("Password must be at least " + minLength + " characters long");
        }
        if (password.length() > maxLength) {
            throw new IllegalArgumentException("Password must not exceed " + maxLength + " characters");
        }

        // 检查大写字母
        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        // 检查小写字母
        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        // 检查数字
        if (requireDigit && !DIGIT_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        // 检查特殊字符
        if (requireSpecial && !SPECIAL_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    /**
     * 检查密码强度
     */
    public PasswordStrength checkStrength(String password) {
        int score = 0;

        if (password.length() >= minLength) score++;
        if (password.length() >= 12) score++;
        if (UPPERCASE_PATTERN.matcher(password).find()) score++;
        if (LOWERCASE_PATTERN.matcher(password).find()) score++;
        if (DIGIT_PATTERN.matcher(password).find()) score++;
        if (SPECIAL_PATTERN.matcher(password).find()) score++;

        if (score <= 2) return PasswordStrength.WEAK;
        if (score <= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }

    /**
     * 密码强度枚举
     */
    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}
