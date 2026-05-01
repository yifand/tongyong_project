package com.vdc.pdi.logmanagement.aspect;

import com.vdc.pdi.common.utils.IpUtils;
import com.vdc.pdi.common.utils.JsonUtils;
import com.vdc.pdi.common.utils.SecurityUtils;
import com.vdc.pdi.logmanagement.domain.entity.OperationLog;
import com.vdc.pdi.logmanagement.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志AOP切面
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    /**
     * 定义切点：所有带有@OperationLog注解的方法
     */
    @Pointcut("@annotation(com.vdc.pdi.logmanagement.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.vdc.pdi.logmanagement.annotation.OperationLog annotation =
                method.getAnnotation(com.vdc.pdi.logmanagement.annotation.OperationLog.class);

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 获取当前用户和IP
        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        String ipAddress = getClientIp();

        // 获取当前站点ID (简化实现，实际应从用户信息中获取)
        Long siteId = 0L;

        // 构建操作日志
        OperationLog operationLog = new OperationLog();
        operationLog.setSiteId(siteId);
        operationLog.setUserId(userId != null ? userId : 0L);
        operationLog.setUsername(username != null ? username : "anonymous");
        operationLog.setIpAddress(ipAddress);
        operationLog.setOperationType(annotation.type().getCode());
        operationLog.setOperationDetail(buildDescription(annotation, joinPoint));

        // 记录请求参数
        if (annotation.recordParams()) {
            String params = buildRequestParams(joinPoint, annotation.excludeParams());
            // 限制长度
            if (params != null && params.length() > 2048) {
                params = params.substring(0, 2048);
            }
            operationLog.setRequestParams(params);
        }

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            // 执行成功
            operationLog.setResult(1);
        } catch (Exception e) {
            // 执行失败
            operationLog.setResult(0);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 1024) {
                errorMsg = errorMsg.substring(0, 1024);
            }
            operationLog.setErrorMsg(errorMsg);
            throw e;
        } finally {
            // 计算执行时长
            long executionTime = System.currentTimeMillis() - startTime;
            operationLog.setExecutionTime(executionTime);

            // 异步保存日志
            operationLogService.saveOperationLogAsync(operationLog);
        }

        return result;
    }

    /**
     * 构建操作描述
     */
    private String buildDescription(com.vdc.pdi.logmanagement.annotation.OperationLog annotation, ProceedingJoinPoint joinPoint) {
        String description = annotation.description();
        if (StringUtils.hasText(description)) {
            return description;
        }
        return annotation.type().getDescription();
    }

    /**
     * 构建请求参数字符串
     */
    private String buildRequestParams(ProceedingJoinPoint joinPoint, String[] excludeParams) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "{}";
            }

            // 过滤敏感参数
            Map<String, Object> params = new HashMap<>();
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                // 忽略HttpServletRequest/Response等类型
                if (arg instanceof HttpServletRequest ||
                    arg instanceof jakarta.servlet.http.HttpServletResponse ||
                    arg instanceof MultipartFile) {
                    continue;
                }

                // 转换为Map并过滤敏感字段
                try {
                    String json = JsonUtils.toJson(arg);
                    if (json != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> argMap = JsonUtils.fromJson(json, java.util.HashMap.class);
                        if (argMap != null) {
                            for (String exclude : excludeParams) {
                                argMap.remove(exclude);
                            }
                            params.putAll(argMap);
                        }
                    }
                } catch (Exception e) {
                    // 如果转换失败，使用toString
                    params.put("arg", arg.toString());
                }
            }

            return JsonUtils.toJson(params);
        } catch (Exception e) {
            log.warn("构建请求参数失败", e);
            return "{}";
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return IpUtils.getClientIp(request);
            }
        } catch (Exception e) {
            log.debug("获取客户端IP失败", e);
        }
        return "";
    }
}
