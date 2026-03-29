
package com.vdc.pdi.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResultCode 单元测试
 */
class ResultCodeTest {

    @Test
    void testFromCode() {
        assertEquals(ResultCode.SUCCESS, ResultCode.fromCode(200));
        assertEquals(ResultCode.BAD_REQUEST, ResultCode.fromCode(400));
        assertEquals(ResultCode.UNAUTHORIZED, ResultCode.fromCode(401));
        assertEquals(ResultCode.INTERNAL_ERROR, ResultCode.fromCode(500));
        assertNull(ResultCode.fromCode(99999));
    }

    @Test
    void testIsSuccess() {
        assertTrue(ResultCode.SUCCESS.isSuccess());
        assertTrue(ResultCode.CREATED.isSuccess());
        assertTrue(ResultCode.ACCEPTED.isSuccess());
        assertTrue(ResultCode.NO_CONTENT.isSuccess());

        assertFalse(ResultCode.BAD_REQUEST.isSuccess());
        assertFalse(ResultCode.INTERNAL_ERROR.isSuccess());
        assertFalse(ResultCode.BIZ_ERROR.isSuccess());
    }

    @Test
    void testIsClientError() {
        assertTrue(ResultCode.BAD_REQUEST.isClientError());
        assertTrue(ResultCode.UNAUTHORIZED.isClientError());
        assertTrue(ResultCode.FORBIDDEN.isClientError());
        assertTrue(ResultCode.NOT_FOUND.isClientError());

        assertFalse(ResultCode.SUCCESS.isClientError());
        assertFalse(ResultCode.INTERNAL_ERROR.isClientError());
    }

    @Test
    void testIsServerError() {
        assertTrue(ResultCode.INTERNAL_ERROR.isServerError());
        assertTrue(ResultCode.SERVICE_UNAVAILABLE.isServerError());

        assertFalse(ResultCode.SUCCESS.isServerError());
        assertFalse(ResultCode.BIZ_ERROR.isServerError());
    }

    @Test
    void testIsBizError() {
        assertTrue(ResultCode.BIZ_ERROR.isBizError());
        assertTrue(ResultCode.AUTH_ERROR.isBizError());
        assertTrue(ResultCode.VALIDATION_ERROR.isBizError());

        assertFalse(ResultCode.SUCCESS.isBizError());
        assertFalse(ResultCode.BAD_REQUEST.isBizError());
        assertFalse(ResultCode.INTERNAL_ERROR.isBizError());
    }

    @Test
    void testGetCodeAndMessage() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
        assertEquals("操作成功", ResultCode.SUCCESS.getMessage());

        assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
        assertEquals("未授权，请先登录", ResultCode.UNAUTHORIZED.getMessage());
    }

    @Test
    void testEnumCodeInterface() {
        EnumCode<Integer> enumCode = ResultCode.SUCCESS;
        assertEquals(200, enumCode.getCode());
        assertEquals("操作成功", enumCode.getMessage());
        assertEquals(200, enumCode.getValue());
    }
}
