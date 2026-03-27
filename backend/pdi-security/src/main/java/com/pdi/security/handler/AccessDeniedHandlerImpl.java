package com.pdi.security.handler;

import com.alibaba.fastjson2.JSON;
import com.pdi.common.result.Result;
import com.pdi.common.result.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 权限不足处理器
 * <p>
 * 处理已认证用户访问无权限资源的情况
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("权限不足 - URI: {}, 用户: {}, 错误: {}", 
                request.getRequestURI(), 
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "unknown",
                accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Result<Void> result = Result.error(ResultCode.FORBIDDEN.getCode(), "没有权限访问该资源");
        response.getWriter().write(JSON.toJSONString(result));
        response.getWriter().flush();
    }
}
