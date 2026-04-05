package com.vdc.pdi.behaviorarchive.exception;

import com.vdc.pdi.common.enums.ResultCode;
import com.vdc.pdi.common.exception.BusinessException;

/**
 * 档案模块业务异常
 */
public class ArchiveException extends BusinessException {

    public ArchiveException(String message) {
        super(ResultCode.ARCHIVE_NOT_FOUND, message);
    }

    public ArchiveException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public ArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
