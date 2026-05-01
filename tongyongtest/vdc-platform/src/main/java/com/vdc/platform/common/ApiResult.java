package com.vdc.platform.common;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResult() {
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(ResultCode.SUCCESS);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> error(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> ApiResult<T> error(String message) {
        return error(ResultCode.ERROR, message);
    }
}
