package com.pdi.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 系统错误
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 认证授权错误 (11xx)
    USERNAME_OR_PASSWORD_ERROR(1101, "用户名或密码错误"),
    ACCOUNT_DISABLED(1102, "账号已被禁用"),
    LOGIN_FAILED_TOO_MANY(1103, "登录失败次数过多，请稍后重试"),
    TOKEN_INVALID(1104, "Token无效或已过期"),
    REFRESH_TOKEN_INVALID(1105, "刷新Token无效"),
    CAPTCHA_ERROR(1106, "验证码错误或已过期"),
    NO_PERMISSION(1107, "无权限执行此操作"),
    DATA_PERMISSION_DENIED(1108, "数据权限不足"),

    // 预警中心错误 (20xx)
    ALARM_NOT_FOUND(2001, "预警不存在"),
    ALARM_ALREADY_HANDLED(2002, "预警已处理，不能重复操作"),
    ALARM_IMAGE_NOT_FOUND(2003, "预警图片不存在"),

    // 行为档案错误 (30xx)
    ARCHIVE_NOT_FOUND(3001, "档案不存在"),
    ARCHIVE_GENERATING(3002, "档案生成中，请稍后下载"),

    // 设备管理错误 (40xx)
    BOX_NOT_FOUND(4001, "盒子不存在"),
    BOX_CODE_EXISTS(4002, "盒子编码已存在"),
    BOX_OFFLINE(4003, "盒子离线，无法操作"),
    CHANNEL_NOT_FOUND(4004, "通道不存在"),
    CHANNEL_CODE_EXISTS(4005, "通道编码已存在"),

    // 系统管理错误 (50xx)
    USER_NOT_FOUND(5001, "用户不存在"),
    USERNAME_EXISTS(5002, "用户名已存在"),
    PHONE_EXISTS(5003, "手机号已存在"),
    ROLE_NOT_FOUND(5004, "角色不存在"),
    ROLE_CODE_EXISTS(5005, "角色编码已存在"),
    CANNOT_DELETE_ADMIN(5006, "不能删除超级管理员"),
    OLD_PASSWORD_ERROR(5007, "旧密码错误"),

    // 系统配置错误 (60xx)
    CONFIG_NOT_FOUND(6001, "配置项不存在"),
    CONFIG_NOT_EDITABLE(6002, "配置项不可编辑"),

    // 文件服务错误 (90xx)
    FILE_UPLOAD_FAILED(9001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(9002, "文件格式不支持"),
    FILE_SIZE_EXCEEDED(9003, "文件大小超过限制");

    private final Integer code;
    private final String message;

}
