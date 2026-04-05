package com.vdc.pdi.algorithminlet.exception;

import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.common.enums.ResultCode;

/**
 * 算法数据入口模块业务异常
 */
public class InletException extends BusinessException {

    public InletException(String message) {
        super(ResultCode.BIZ_ERROR, message);
    }

    public InletException(ResultCode resultCode) {
        super(resultCode);
    }

    public InletException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
