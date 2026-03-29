
package com.vdc.pdi.common.exception;

import com.vdc.pdi.common.enums.ResultCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异常类单元测试
 */
class ExceptionTest {

    @Test
    void testBizException() {
        BizException ex = new BizException("业务错误");
        assertEquals("业务错误", ex.getMessage());
        assertEquals(ResultCode.BIZ_ERROR, ex.getResultCode());
        assertNull(ex.getData());

        BizException ex2 = new BizException(ResultCode.DATA_NOT_FOUND);
        assertEquals(ResultCode.DATA_NOT_FOUND.getMessage(), ex2.getMessage());
        assertEquals(ResultCode.DATA_NOT_FOUND, ex2.getResultCode());

        BizException ex3 = new BizException(ResultCode.INTERNAL_ERROR, "自定义消息");
        assertEquals("自定义消息", ex3.getMessage());
        assertEquals(ResultCode.INTERNAL_ERROR, ex3.getResultCode());

        Object data = Map.of("key", "value");
        BizException ex4 = new BizException(ResultCode.BIZ_ERROR, "消息", data);
        assertEquals(data, ex4.getData());
    }

    @Test
    void testAuthException() {
        AuthException ex = new AuthException("认证失败");
        assertEquals("认证失败", ex.getMessage());
        assertEquals(ResultCode.AUTH_ERROR, ex.getResultCode());

        AuthException ex2 = new AuthException(ResultCode.TOKEN_EXPIRED);
        assertEquals(ResultCode.TOKEN_EXPIRED, ex2.getResultCode());

        AuthException ex3 = new AuthException(ResultCode.PERMISSION_DENIED, "权限不足");
        assertEquals("权限不足", ex3.getMessage());
        assertEquals(ResultCode.PERMISSION_DENIED, ex3.getResultCode());
    }

    @Test
    void testValidationException() {
        ValidationException ex = new ValidationException("校验失败");
        assertEquals("校验失败", ex.getMessage());
        assertEquals(ResultCode.VALIDATION_ERROR, ex.getResultCode());
        assertNull(ex.getErrors());

        Map<String, String> errors = Map.of("field1", "错误1", "field2", "错误2");
        ValidationException ex2 = new ValidationException(errors);
        assertEquals(errors, ex2.getErrors());
        assertEquals("参数校验失败", ex2.getMessage());

        ValidationException ex3 = new ValidationException("name", "名称不能为空");
        assertEquals(Map.of("name", "名称不能为空"), ex3.getErrors());
    }
}
