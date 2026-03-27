package com.pdi.security.filter;

import com.pdi.security.component.JwtTokenProvider;
import com.pdi.security.service.SecurityUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * <p>
 * 从请求头中解析JWT令牌，验证并设置SecurityContext
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                // 验证并解析令牌
                Claims claims = tokenProvider.validateToken(token);
                
                // 检查是否是刷新令牌（刷新令牌不能用于访问）
                if (tokenProvider.isRefreshToken(token)) {
                    log.warn("尝试使用刷新令牌访问受保护资源: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                String userId = claims.getSubject();

                // 加载用户详情
                SecurityUser userDetails = (SecurityUser) userDetailsService.loadUserByUsername(userId);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 设置SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT认证成功 - 用户: {}, URI: {}", userDetails.getUsername(), request.getRequestURI());
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            request.setAttribute("jwt_exception", "TOKEN_EXPIRED");
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("JWT令牌无效: {}", e.getMessage());
            request.setAttribute("jwt_exception", "TOKEN_INVALID");
        } catch (Exception e) {
            log.error("JWT认证过程中发生错误: {}", e.getMessage(), e);
            request.setAttribute("jwt_exception", "AUTH_ERROR");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT令牌
     *
     * @param request HTTP请求
     * @return JWT令牌，如果不存在则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 对登录和刷新接口跳过JWT过滤
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") 
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/api/receiver/")
                || path.startsWith("/ws/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/actuator/health");
    }
}
