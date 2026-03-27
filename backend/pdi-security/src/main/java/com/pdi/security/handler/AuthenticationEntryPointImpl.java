package com.pdi.security.handler;

import com.alibaba.fastjson2.JSON;
import com.pdi.common.result.Result;
import com.pdi.common.result.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证失败处理器
 * <p>
 * 处理未认证用户访问受保护资源的情况
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("认证失败 - URI: {}, 错误: {}", request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 检查是否有JWT异常信息
        String jwtException = (String) request.getAttribute("jwt_exception");
        ResultCode resultCode = ResultCode.UNAUTHORIZED;
        String message = "未登录或登录已过期";

        if ("TOKEN_EXPIRED".equals(jwtException)) {
            resultCode = ResultCode.TOKEN_INVALID;
            message = "登录已过期，请重新登录";
        } else if ("TOKEN_INVALID".equals(jwtException)) {
            resultCode = ResultCode.TOKEN_INVALID;
            message = "无效的登录凭证";
        }

        Result<Void> result = Result.error(resultCode.getCode(), message);
        response.getWriter().write(JSON.toJSONString(result));
        response.getWriter().flush();
    }
}
