package com.vdc.pdi.ruleengine.exception;

import com.vdc.pdi.common.exception.BizException;
import com.vdc.pdi.common.enums.ResultCode;

/**
 * 规则引擎异常
 */
public class RuleEngineException extends BizException {

    public RuleEngineException(String message) {
        super(message);
    }

    public RuleEngineException(ResultCode resultCode) {
        super(resultCode);
    }

    public RuleEngineException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
