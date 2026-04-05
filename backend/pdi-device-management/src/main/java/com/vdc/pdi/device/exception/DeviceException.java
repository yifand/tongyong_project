package com.vdc.pdi.device.exception;

import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.common.enums.ResultCode;

/**
 * 设备管理模块业务异常
 */
public class DeviceException extends BusinessException {

    public DeviceException(String message) {
        super(ResultCode.BIZ_ERROR, message);
    }

    public DeviceException(ResultCode resultCode) {
        super(resultCode);
    }

    public DeviceException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
