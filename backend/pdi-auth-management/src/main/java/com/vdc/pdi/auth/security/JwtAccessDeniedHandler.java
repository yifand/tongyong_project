package com.vdc.pdi.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.enums.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 访问拒绝处理器
 * 处理已认证但无权限访问的情况
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        logger.warn("访问被拒绝 [{}]: {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<Void> apiResponse = ApiResponse.error(
                ResultCode.FORBIDDEN,
                "权限不足：" + accessDeniedException.getMessage()
        );

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
