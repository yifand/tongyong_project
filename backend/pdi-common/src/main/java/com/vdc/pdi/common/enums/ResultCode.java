
package com.vdc.pdi.common.enums;

/**
 * 响应码枚举
 * 定义系统统一的响应状态码
 */
public enum ResultCode implements EnumCode<Integer> {

    // ========== 成功响应 ==========
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    ACCEPTED(202, "请求已接受"),
    NO_CONTENT(204, "无内容返回"),

    // ========== 客户端错误 (4xx) ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "拒绝访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    UNPROCESSABLE_ENTITY(422, "请求参数校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // ========== 服务端错误 (5xx) ==========
    INTERNAL_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // ========== 业务错误 (6xx) ==========
    BIZ_ERROR(600, "业务处理失败"),
    DATA_NOT_FOUND(601, "数据不存在"),
    DATA_ALREADY_EXISTS(602, "数据已存在"),
    DATA_INVALID(603, "数据无效"),
    DATA_EXPIRED(604, "数据已过期"),

    // ========== 认证错误 (7xx) ==========
    AUTH_ERROR(700, "认证失败"),
    TOKEN_EXPIRED(701, "令牌已过期"),
    TOKEN_INVALID(702, "令牌无效"),
    PERMISSION_DENIED(703, "权限不足"),

    // ========== 校验错误 (8xx) ==========
    VALIDATION_ERROR(800, "参数校验失败"),

    // ========== 站点错误 (9xx) ==========
    SITE_ERROR(900, "站点相关错误"),
    SITE_NOT_FOUND(901, "站点不存在"),

    // ========== 设备错误 (10xx) ==========
    DEVICE_ERROR(1000, "设备相关错误"),
    DEVICE_NOT_FOUND(1001, "设备不存在"),
    DEVICE_OFFLINE(1002, "设备离线"),
    DEVICE_COMMUNICATION_ERROR(1003, "设备通信失败"),

    // ========== 归档错误 (11xx) ==========
    ARCHIVE_ERROR(1100, "归档相关错误"),
    ARCHIVE_NOT_FOUND(1101, "归档不存在"),

    // ========== 测点错误 (12xx) ==========
    POINT_ERROR(1200, "测点相关错误"),
    POINT_NOT_FOUND(1201, "测点不存在"),

    // ========== 告警错误 (13xx) ==========
    ALARM_ERROR(1300, "告警相关错误"),
    ALARM_NOT_FOUND(1301, "告警不存在"),

    // ========== 状态码错误 (14xx) ==========
    STATE_CODE_ERROR(1400, "状态码相关错误"),
    STATE_CODE_NOT_FOUND(1401, "状态码不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 根据编码获取枚举
     */
    public static ResultCode fromCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.code.equals(code)) {
                return resultCode;
            }
        }
        return null;
    }

    /**
     * 判断是否成功响应
     */
    public boolean isSuccess() {
        return this.code >= 200 && this.code < 300;
    }

    /**
     * 判断是否为客户端错误
     */
    public boolean isClientError() {
        return this.code >= 400 && this.code < 500;
    }

    /**
     * 判断是否为服务端错误
     */
    public boolean isServerError() {
        return this.code >= 500 && this.code < 600;
    }

    /**
     * 判断是否为业务错误
     */
    public boolean isBizError() {
        return this.code >= 600;
    }
}
