package com.pdi.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;

/**
 * 安全工具类
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * BCrypt加密密码
     */
    public static String encryptPassword(String password) {
        if (StrUtil.isBlank(password)) {
            return null;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * 验证密码
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(encodedPassword)) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

}
