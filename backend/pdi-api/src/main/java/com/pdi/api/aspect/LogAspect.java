package com.pdi.api.aspect;

import com.pdi.common.result.Result;
import com.pdi.common.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    @Pointcut("@annotation(com.pdi.api.aspect.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();

        // 执行方法
        Object result = point.proceed();

        // 执行时长
        long time = System.currentTimeMillis() - beginTime;

        // 保存日志
        saveLog(point, time, result);

        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        // 获取request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        // 构建日志实体
        LogInfo logInfo = new LogInfo();
        logInfo.setModule(annotation.module());
        logInfo.setOperation(annotation.operation());
        logInfo.setRequestMethod(request.getMethod());
        logInfo.setRequestUri(request.getRequestURI());
        logInfo.setClassMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
        logInfo.setIpAddress(IpUtils.getIpAddr(request));
        logInfo.setUserAgent(request.getHeader("User-Agent"));
        logInfo.setExecuteTime(time);

        // TODO: 从SecurityContext获取当前用户信息
        // Long userId = SecurityUtils.getCurrentUserId();
        // String username = SecurityUtils.getCurrentUsername();
        logInfo.setUserId(null);
        logInfo.setUsername("anonymous");

        // 请求参数
        if (annotation.saveParams()) {
            logInfo.setRequestParams(getRequestParams(joinPoint));
        }

        // 响应结果
        if (annotation.saveResult() && result != null) {
            if (result instanceof Result) {
                logInfo.setStatus(((Result<?>) result).isSuccess() ? 1 : 0);
            }
        }

        // 异步保存日志（这里只打印日志，实际项目中可以保存到数据库）
        log.info("操作日志 - 模块: {}, 操作: {}, 用户: {}, IP: {}, 耗时: {}ms",
                logInfo.getModule(),
                logInfo.getOperation(),
                logInfo.getUsername(),
                logInfo.getIpAddress(),
                logInfo.getExecuteTime());

        // TODO: 调用LogService异步保存日志
        // logService.saveAsync(logInfo);
    }

    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }

        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            if (arg != null && !(arg instanceof HttpServletRequest)) {
                params.append(arg.toString()).append(";");
            }
        }
        return params.toString();
    }

    /**
     * 日志信息内部类
     */
    @lombok.Data
    public static class LogInfo {
        private String module;
        private String operation;
        private String requestMethod;
        private String requestUri;
        private String classMethod;
        private String requestParams;
        private String ipAddress;
        private String userAgent;
        private Long userId;
        private String username;
        private Long executeTime;
        private Integer status;
    }
}
