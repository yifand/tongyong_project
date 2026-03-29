package com.vdc.pdi.common.exception;

import com.vdc.pdi.common.enums.ResultCode;

/**
 * 认证异常
 */
public class AuthException extends BizException {

    public AuthException(String message) {
        super(ResultCode.AUTH_ERROR, message);
    }

    public AuthException(ResultCode resultCode) {
        super(resultCode);
    }

    public AuthException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
