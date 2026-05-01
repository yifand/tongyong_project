package com.vdc.pdi.logmanagement.aspect;

import com.vdc.pdi.logmanagement.enums.OperationType;
import com.vdc.pdi.logmanagement.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 操作日志切面测试类
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OperationLogAspectTest {

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private OperationLogAspect operationLogAspect;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    /**
     * OA-001: 正常方法执行记录日志
     */
    @Test
    void testAround_Success() throws Throwable {
        // 准备测试方法
        TestService testService = new TestService();
        java.lang.reflect.Method method = testService.getClass().getMethod("testMethod", String.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getDeclaringType()).thenReturn(TestService.class);
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"testParam"});

        // 执行切面
        Object result = operationLogAspect.around(joinPoint);

        // 验证方法执行成功
        assertThat(result).isEqualTo("success");

        // 验证异步保存日志被调用
        verify(operationLogService, times(1)).saveOperationLogAsync(any(com.vdc.pdi.logmanagement.domain.entity.OperationLog.class));

        // 捕获保存的日志对象进行验证
        ArgumentCaptor<com.vdc.pdi.logmanagement.domain.entity.OperationLog> logCaptor =
            ArgumentCaptor.forClass(com.vdc.pdi.logmanagement.domain.entity.OperationLog.class);
        verify(operationLogService).saveOperationLogAsync(logCaptor.capture());
        com.vdc.pdi.logmanagement.domain.entity.OperationLog savedLog = logCaptor.getValue();

        assertThat(savedLog.getResult()).isEqualTo(1); // 成功
        assertThat(savedLog.getOperationType()).isEqualTo(1); // 登录
        assertThat(savedLog.getExecutionTime()).isNotNull();
        assertThat(savedLog.getExecutionTime()).isGreaterThanOrEqualTo(0);
    }

    /**
     * OA-002: 方法执行异常记录日志
     */
    @Test
    void testAround_Exception() throws Throwable {
        // 准备测试方法
        TestService testService = new TestService();
        java.lang.reflect.Method method = testService.getClass().getMethod("testMethod", String.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{"testParam"});

        // 执行切面，应该抛出异常
        assertThrows(RuntimeException.class, () -> operationLogAspect.around(joinPoint));

        // 验证异步保存日志被调用
        verify(operationLogService, times(1)).saveOperationLogAsync(any(com.vdc.pdi.logmanagement.domain.entity.OperationLog.class));

        // 捕获保存的日志对象进行验证
        ArgumentCaptor<com.vdc.pdi.logmanagement.domain.entity.OperationLog> logCaptor =
            ArgumentCaptor.forClass(com.vdc.pdi.logmanagement.domain.entity.OperationLog.class);
        verify(operationLogService).saveOperationLogAsync(logCaptor.capture());
        com.vdc.pdi.logmanagement.domain.entity.OperationLog savedLog = logCaptor.getValue();

        assertThat(savedLog.getResult()).isEqualTo(0); // 失败
        assertThat(savedLog.getErrorMsg()).isEqualTo("Test exception");
    }

    /**
     * OA-004: 自定义操作描述
     */
    @Test
    void testAround_CustomDescription() throws Throwable {
        // 准备带自定义描述的方法
        TestService testService = new TestService();
        java.lang.reflect.Method method = testService.getClass().getMethod("testMethodWithDescription", String.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});

        // 执行切面
        operationLogAspect.around(joinPoint);

        // 捕获保存的日志对象进行验证
        ArgumentCaptor<com.vdc.pdi.logmanagement.domain.entity.OperationLog> logCaptor =
            ArgumentCaptor.forClass(com.vdc.pdi.logmanagement.domain.entity.OperationLog.class);
        verify(operationLogService).saveOperationLogAsync(logCaptor.capture());
        com.vdc.pdi.logmanagement.domain.entity.OperationLog savedLog = logCaptor.getValue();

        assertThat(savedLog.getOperationDetail()).isEqualTo("自定义操作描述");
    }

    /**
     * OA-005: 禁用参数记录
     */
    @Test
    void testAround_NoParams() throws Throwable {
        // 准备禁用参数记录的方法
        TestService testService = new TestService();
        java.lang.reflect.Method method = testService.getClass().getMethod("testMethodNoParams", String.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});

        // 执行切面
        operationLogAspect.around(joinPoint);

        // 捕获保存的日志对象进行验证
        ArgumentCaptor<com.vdc.pdi.logmanagement.domain.entity.OperationLog> logCaptor =
            ArgumentCaptor.forClass(com.vdc.pdi.logmanagement.domain.entity.OperationLog.class);
        verify(operationLogService).saveOperationLogAsync(logCaptor.capture());
        com.vdc.pdi.logmanagement.domain.entity.OperationLog savedLog = logCaptor.getValue();

        // 验证请求参数为空
        assertThat(savedLog.getRequestParams()).isNull();
    }

    /**
     * 测试用的服务类
     */
    static class TestService {
        @com.vdc.pdi.logmanagement.annotation.OperationLog(type = OperationType.LOGIN)
        public String testMethod(String param) {
            return "success";
        }

        @com.vdc.pdi.logmanagement.annotation.OperationLog(type = OperationType.LOGIN, description = "自定义操作描述")
        public String testMethodWithDescription(String param) {
            return "success";
        }

        @com.vdc.pdi.logmanagement.annotation.OperationLog(type = OperationType.LOGIN, recordParams = false)
        public String testMethodNoParams(String param) {
            return "success";
        }
    }
}
