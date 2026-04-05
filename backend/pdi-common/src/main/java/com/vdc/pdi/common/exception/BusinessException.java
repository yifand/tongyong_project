package com.vdc.pdi.common.exception;

import com.vdc.pdi.common.enums.ResultCode;

/**
 * 业务异常 - BusinessException别名
 * 为了兼容引用BusinessException的代码
 */
public class BusinessException extends BizException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(String code, String message) {
        super(ResultCode.BIZ_ERROR, message);
    }
}
