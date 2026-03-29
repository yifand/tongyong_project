
package com.vdc.pdi.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse 单元测试
 */
class ApiResponseTest {

    @Test
    void testSuccessWithoutData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessWithData() {
        String testData = "test data";
        ApiResponse<String> response = ApiResponse.success(testData);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertEquals(testData, response.getData());
    }

    @Test
    void testSuccessWithCustomMessage() {
        String customMessage = "自定义成功消息";
        ApiResponse<Void> response = ApiResponse.success(customMessage);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(customMessage, response.getMessage());
    }

    @Test
    void testError() {
        ApiResponse<Void> response = ApiResponse.error();

        assertNotNull(response);
        assertEquals(600, response.getCode());
        assertEquals("操作失败", response.getMessage());
    }

    @Test
    void testErrorWithCustomMessage() {
        String errorMessage = "自定义错误消息";
        ApiResponse<Void> response = ApiResponse.error(errorMessage);

        assertNotNull(response);
        assertEquals(600, response.getCode());
        assertEquals(errorMessage, response.getMessage());
    }

    @Test
    void testErrorWithCodeAndMessage() {
        int errorCode = 400;
        String errorMessage = "参数错误";
        ApiResponse<Void> response = ApiResponse.error(errorCode, errorMessage);

        assertNotNull(response);
        assertEquals(errorCode, response.getCode());
        assertEquals(errorMessage, response.getMessage());
    }

    @Test
    void testIsSuccess() {
        ApiResponse<Void> successResponse = ApiResponse.success();
        ApiResponse<Void> errorResponse = ApiResponse.error();

        assertTrue(successResponse.isSuccess());
        assertFalse(errorResponse.isSuccess());
    }

    @Test
    void testSettersAndGetters() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("测试");
        response.setData("data");
        response.setTimestamp(123456789L);

        assertEquals(200, response.getCode());
        assertEquals("测试", response.getMessage());
        assertEquals("data", response.getData());
        assertEquals(123456789L, response.getTimestamp());
    }
}
