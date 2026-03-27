package com.pdi.common.constant;

/**
 * 缓存常量
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public final class CacheConstants {

    private CacheConstants() {
    }

    /**
     * 缓存前缀
     */
    public static final String CACHE_PREFIX = "pdi:";

    /**
     * 用户缓存
     */
    public static final String USER_CACHE = CACHE_PREFIX + "user:";

    /**
     * 角色缓存
     */
    public static final String ROLE_CACHE = CACHE_PREFIX + "role:";

    /**
     * 权限缓存
     */
    public static final String PERMISSION_CACHE = CACHE_PREFIX + "permission:";

    /**
     * 验证码缓存
     */
    public static final String CAPTCHA_CACHE = CACHE_PREFIX + "captcha:";

    /**
     * Token缓存
     */
    public static final String TOKEN_CACHE = CACHE_PREFIX + "token:";

    /**
     * 设备状态缓存
     */
    public static final String DEVICE_STATUS_CACHE = CACHE_PREFIX + "device:status:";

    /**
     * 在线用户缓存
     */
    public static final String ONLINE_USER_CACHE = CACHE_PREFIX + "online:user:";

}
