package com.vdc.pdi.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vdc.pdi.common.enums.ResultCode;

import java.time.Instant;

/**
 * 统一API响应结构
 *
 * @param <T> 数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    // 私有构造器
    private ApiResponse() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    private ApiResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 成功响应（自定义消息+数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应（默认业务错误）
     */
    public static <T> ApiResponse<T> error() {
        return error(ResultCode.BIZ_ERROR);
    }

    /**
     * 失败响应（自定义消息，默认业务错误码）
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ResultCode.BIZ_ERROR.getCode(), message, null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        return new ApiResponse<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> ApiResponse<T> error(ResultCode resultCode, String message) {
        return new ApiResponse<>(resultCode.getCode(), message, null);
    }

    /**
     * 失败响应（自定义码和消息）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
