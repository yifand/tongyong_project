package com.pdi.common.constant;

/**
 * 系统常量
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public final class SystemConstants {

    private SystemConstants() {
    }

    /**
     * 超级管理员ID
     */
    public static final Long SUPER_ADMIN_ID = 1L;

    /**
     * 超级管理员角色编码
     */
    public static final String SUPER_ADMIN_ROLE = "admin";

    /**
     * JWT Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头 - Token
     */
    public static final String HEADER_TOKEN = "Authorization";

    /**
     * 请求头 - 站点ID
     */
    public static final String HEADER_SITE_ID = "X-Site-Id";

    /**
     * 请求头 - 请求ID
     */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    /**
     * 默认页码
     */
    public static final long DEFAULT_PAGE_NUM = 1L;

    /**
     * 默认每页大小
     */
    public static final long DEFAULT_PAGE_SIZE = 20L;

    /**
     * 最大每页大小
     */
    public static final long MAX_PAGE_SIZE = 100L;

}
