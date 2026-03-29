
package com.vdc.pdi.common.constant;

/**
 * 公共常量定义
 */
public final class CommonConstant {

    private CommonConstant() {
        // 私有构造，防止实例化
    }

    // ========== 系统相关 ==========

    /**
     * 应用名称
     */
    public static final String APP_NAME = "PDI智能监测平台";

    /**
     * 默认时区
     */
    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    /**
     * 默认日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 默认日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期时间格式（带毫秒）
     */
    public static final String DATETIME_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    // ========== 分页相关 ==========

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页大小
     */
    public static final int MAX_PAGE_SIZE = 1000;

    // ========== 缓存相关 ==========

    /**
     * 默认缓存过期时间（分钟）
     */
    public static final long DEFAULT_CACHE_EXPIRE_MINUTES = 30;

    /**
     * 用户缓存前缀
     */
    public static final String CACHE_USER_PREFIX = "user:";

    /**
     * 字典缓存前缀
     */
    public static final String CACHE_DICT_PREFIX = "dict:";

    /**
     * 站点缓存前缀
     */
    public static final String CACHE_SITE_PREFIX = "site:";

    // ========== 安全相关 ==========

    /**
     * Token请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求ID请求头名称
     */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * 站点ID请求头名称
     */
    public static final String SITE_ID_HEADER = "X-Site-ID";

    // ========== 文件相关 ==========

    /**
     * 默认文件编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 临时文件目录
     */
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    // ========== 特殊值 ==========

    /**
     * 未知值
     */
    public static final String UNKNOWN = "unknown";

    /**
     * 成功标记
     */
    public static final String SUCCESS = "success";

    /**
     * 失败标记
     */
    public static final String FAIL = "fail";

    // ========== 数值常量 ==========

    /**
     * 字节缓冲区大小
     */
    public static final int BUFFER_SIZE = 8192;

    /**
     * 秒转毫秒
     */
    public static final int SECOND_MILLIS = 1000;

    /**
     * 分转秒
     */
    public static final int MINUTE_SECONDS = 60;

    /**
     * 时转分
     */
    public static final int HOUR_MINUTES = 60;

    /**
     * 天转时
     */
    public static final int DAY_HOURS = 24;
}
