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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证入口点
 * 处理认证失败的情况（如未登录、Token无效等）
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        logger.warn("认证失败 [{}]: {}", request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> apiResponse = ApiResponse.error(
                ResultCode.UNAUTHORIZED,
                "认证失败：" + authException.getMessage()
        );

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
