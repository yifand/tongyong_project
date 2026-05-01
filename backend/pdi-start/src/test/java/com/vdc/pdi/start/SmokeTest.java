package com.vdc.pdi.start;

import com.vdc.pdi.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PDI智能监测平台 - 冒烟测试
 *
 * 验证系统基本功能是否正常:
 * 1. 服务启动测试
 * 2. 数据库连接测试
 * 3. 登录认证测试
 * 4. 受保护接口测试
 * 5. 设备管理CRUD测试
 * 6. 全局异常处理测试
 * 7. 跨域配置测试
 * 8. API文档访问测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/sql/smoke-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("PDI智能监测平台冒烟测试")
class SmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataSource dataSource;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    @DisplayName("TC001: 应用上下文加载测试")
    void contextLoads() {
        // 如果应用上下文无法加载，此测试会自动失败
        assertTrue(true, "应用上下文加载成功");
    }

    @Test
    @DisplayName("TC002: 数据库连接测试")
    void testDatabaseConnection() throws SQLException {
        // 验证数据源配置正确
        assertNotNull(dataSource, "数据源不应为空");

        // 验证可以获取数据库连接
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应为空");
            assertFalse(connection.isClosed(), "数据库连接应该处于打开状态");
        }
    }

    @Test
    @DisplayName("TC003: 登录认证测试 - 正确凭据")
    void testLoginWithValidCredentials() {
        // 准备登录请求
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "Test1234");

        // 发送登录请求
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/login",
                loginRequest,
                ApiResponse.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode(), "登录应该返回200状态码");
        assertNotNull(response.getBody(), "响应体不应为空");
        assertEquals(200, response.getBody().getCode(), "业务状态码应该为200");
        assertNotNull(response.getBody().getData(), "响应数据不应为空(包含JWT Token)");
    }

    @Test
    @DisplayName("TC004: 登录认证测试 - 错误凭据")
    void testLoginWithInvalidCredentials() {
        // 准备错误的登录请求
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "wrongpassword");

        // 发送登录请求
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/login",
                loginRequest,
                ApiResponse.class
        );

        // 验证响应 - 应该返回401
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "错误凭据应该返回401状态码");
    }

    @Test
    @DisplayName("TC005: 受保护接口测试 - 未认证访问应被拒绝")
    void testProtectedEndpointWithoutAuth() {
        // 发送不带Token的请求到受保护接口
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/devices/boxes",
                ApiResponse.class
        );

        // 验证响应 - 应该返回401或403
        assertTrue(
                response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                        response.getStatusCode() == HttpStatus.FORBIDDEN,
                "未认证访问受保护接口应该返回401或403状态码"
        );
    }

    @Test
    @DisplayName("TC006: 受保护接口测试 - 带Token访问应成功")
    void testProtectedEndpointWithAuth() {
        // 1. 先登录获取Token
        String token = loginAndGetToken();

        // 2. 构建带Token的请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 3. 发送带Token的请求
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/devices/boxes",
                HttpMethod.GET,
                entity,
                ApiResponse.class
        );

        // 4. 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode(), "带Token访问应该返回200状态码");
        assertNotNull(response.getBody(), "响应体不应为空");
        assertEquals(200, response.getBody().getCode(), "业务状态码应该为200");
    }

    @Test
    @DisplayName("TC007: 设备管理CRUD测试 - 查询盒子列表")
    void testDeviceBoxListQuery() {
        // 获取Token
        String token = loginAndGetToken();

        // 构建请求
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送请求
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/devices/boxes?page=1&size=10",
                HttpMethod.GET,
                entity,
                ApiResponse.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
    }

    @Test
    @DisplayName("TC008: 全局异常处理测试 - 资源不存在")
    void testGlobalExceptionHandler_NotFound() {
        // 获取Token
        String token = loginAndGetToken();

        // 构建请求 - 访问不存在的资源
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送请求到不存在的端点
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/devices/boxes/99999",
                HttpMethod.GET,
                entity,
                ApiResponse.class
        );

        // 验证返回标准错误格式
        assertNotNull(response.getBody(), "即使出错也应该返回标准响应格式");
        assertNotNull(response.getBody().getCode(), "响应应该包含错误码");
        assertNotNull(response.getBody().getMessage(), "响应应该包含错误消息");
    }

    @Test
    @DisplayName("TC009: CORS配置测试 - 预检请求")
    void testCorsConfiguration() {
        // 构建CORS预检请求
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://localhost:3000");
        headers.setAccessControlRequestMethod(HttpMethod.GET);
        headers.setAccessControlRequestHeaders(Arrays.asList("Authorization", "Content-Type"));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送OPTIONS请求
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/auth/login",
                HttpMethod.OPTIONS,
                entity,
                String.class
        );

        // 验证CORS响应头
        assertEquals(HttpStatus.OK, response.getStatusCode(), "CORS预检请求应该返回200");
        assertNotNull(response.getHeaders().getAccessControlAllowOrigin(),
                "响应应该包含Access-Control-Allow-Origin头");
    }

    @Test
    @DisplayName("TC010: API文档访问测试 - Swagger UI")
    void testSwaggerUiAccess() {
        // 发送请求到Swagger UI
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/swagger-ui.html",
                String.class
        );

        // 验证响应 - 应该返回HTML页面(即使是重定向也应该成功)
        assertTrue(
                response.getStatusCode() == HttpStatus.OK ||
                        response.getStatusCode() == HttpStatus.FOUND ||
                        response.getStatusCode() == HttpStatus.MOVED_PERMANENTLY,
                "Swagger UI应该可访问"
        );
    }

    @Test
    @DisplayName("TC011: API文档访问测试 - OpenAPI JSON")
    void testOpenApiJsonAccess() {
        // 发送请求到OpenAPI JSON端点
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/v3/api-docs",
                String.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode(), "OpenAPI JSON应该可访问");
        assertNotNull(response.getBody(), "响应体不应为空");
        assertTrue(response.getBody().contains("openapi"), "响应应该包含OpenAPI规范");
    }

    @Test
    @DisplayName("TC012: 健康检查端点测试")
    void testHealthEndpoint() {
        // 发送请求到健康检查端点
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/actuator/health",
                String.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode(), "健康检查端点应该返回200");
        assertNotNull(response.getBody(), "响应体不应为空");
        assertTrue(response.getBody().contains("UP") || response.getBody().contains("DOWN"),
                "响应应该包含健康状态");
    }

    @Test
    @DisplayName("TC013: 预警中心接口测试 - 查询报警列表")
    void testAlarmCenterQuery() {
        // 获取Token
        String token = loginAndGetToken();

        // 构建请求
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送请求
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/alarms/history?page=1&size=10",
                HttpMethod.GET,
                entity,
                ApiResponse.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
    }

    @Test
    @DisplayName("TC014: 行为档案接口测试 - 查询档案列表")
    void testBehaviorArchiveQuery() {
        // 获取Token
        String token = loginAndGetToken();

        // 构建请求
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送请求
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/archives?page=1&size=10",
                HttpMethod.GET,
                entity,
                ApiResponse.class
        );

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
    }

    /**
     * 辅助方法：登录并获取JWT Token
     */
    private String loginAndGetToken() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "Test1234");

        @SuppressWarnings("unchecked")
        ApiResponse<Map<String, Object>> response = restTemplate.postForObject(
                baseUrl + "/api/v1/auth/login",
                loginRequest,
                ApiResponse.class
        );

        assertNotNull(response, "登录响应不应为空");
        assertNotNull(response.getData(), "登录响应数据不应为空");

        @SuppressWarnings("unchecked")
        Map<String, Object> data = response.getData();
        String token = (String) data.get("token");
        assertNotNull(token, "Token不应为空");

        return token;
    }

}
