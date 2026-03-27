package com.pdi.security.component;

import com.pdi.security.service.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 提供者
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:PDISmartMonitoringPlatformSecretKey2026}")
    private String jwtSecret;

    @Value("${jwt.expiration:7200000}")
    private long jwtExpiration; // 2小时 = 7200000毫秒

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration; // 7天 = 604800000毫秒

    private SecretKey key;

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_SITE_ID = "siteId";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_REFRESH = "refresh";

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌
     *
     * @param user 安全用户对象
     * @return JWT访问令牌
     */
    public String generateToken(SecurityUser user) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_SITE_ID, user.getSiteId())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 生成访问令牌（带额外声明）
     *
     * @param user 安全用户对象
     * @param extraClaims 额外声明
     * @return JWT访问令牌
     */
    public String generateToken(SecurityUser user, Map<String, Object> extraClaims) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        var builder = Jwts.builder()
                .subject(user.getUserId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_SITE_ID, user.getSiteId())
                .issuedAt(new Date())
                .expiration(expiryDate);

        // 添加额外声明
        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        return builder.signWith(key).compact();
    }

    /**
     * 生成刷新令牌
     *
     * @param user 安全用户对象
     * @return JWT刷新令牌
     */
    public String generateRefreshToken(SecurityUser user) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshExpiration);

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 验证并解析令牌
     *
     * @param token JWT令牌
     * @return Claims对象
     * @throws ExpiredJwtException 令牌过期
     * @throws JwtException 令牌无效
     */
    public Claims validateToken(String token) throws ExpiredJwtException, JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从令牌获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_USERNAME, String.class);
    }

    /**
     * 检查令牌是否为刷新令牌
     *
     * @param token JWT令牌
     * @return 是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateToken(token);
            return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取令牌过期时间
     *
     * @return 过期时间（毫秒）
     */
    public long getExpiration() {
        return jwtExpiration;
    }

    /**
     * 获取刷新令牌过期时间
     *
     * @return 过期时间（毫秒）
     */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    /**
     * 从令牌获取所有声明
     *
     * @param token JWT令牌
     * @return 声明Map
     */
    public Map<String, Object> getClaimsFromToken(String token) {
        Claims claims = validateToken(token);
        Map<String, Object> result = new HashMap<>();
        claims.forEach(result::put);
        return result;
    }
}
