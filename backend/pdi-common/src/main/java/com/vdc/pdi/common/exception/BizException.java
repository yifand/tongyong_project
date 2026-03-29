package com.vdc.pdi.common.exception;

import com.vdc.pdi.common.enums.ResultCode;

/**
 * 业务异常基类
 */
public class BizException extends RuntimeException {

    private final ResultCode resultCode;
    private final Object data;

    public BizException(String message) {
        super(message);
        this.resultCode = ResultCode.BIZ_ERROR;
        this.data = null;
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.data = null;
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
        this.data = null;
    }

    public BizException(ResultCode resultCode, String message, Object data) {
        super(message);
        this.resultCode = resultCode;
        this.data = data;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public Object getData() {
        return data;
    }
}
