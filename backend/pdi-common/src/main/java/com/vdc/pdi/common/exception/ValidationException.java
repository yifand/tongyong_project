package com.vdc.pdi.common.exception;

import com.vdc.pdi.common.enums.ResultCode;

import java.util.Map;

/**
 * 参数校验异常
 */
public class ValidationException extends BizException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(ResultCode.VALIDATION_ERROR, message);
        this.errors = null;
    }

    public ValidationException(Map<String, String> errors) {
        super(ResultCode.VALIDATION_ERROR, "参数校验失败", errors);
        this.errors = errors;
    }

    public ValidationException(String field, String message) {
        super(ResultCode.VALIDATION_ERROR, message);
        this.errors = Map.of(field, message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
