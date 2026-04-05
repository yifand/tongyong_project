package com.vdc.pdi.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 从请求中提取JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            // 检查Token是否存在且不在黑名单中
            if (StringUtils.hasText(jwt) && !tokenBlacklistService.isBlacklisted(jwt)) {
                // 验证Token
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.extractUsername(jwt);

                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 登录和注册接口不需要JWT验证
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}
