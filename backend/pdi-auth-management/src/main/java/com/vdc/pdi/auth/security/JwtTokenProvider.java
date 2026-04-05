package com.vdc.pdi.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token 提供者
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:mySecretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration; // 默认24小时

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration; // 默认7天

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从Token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查Token是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成Token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * 生成Token（带额外声明）
     */
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 验证Token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * 从Token中获取过期时间（毫秒）
     */
    public long getExpirationTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 从认证信息生成Token
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    /**
     * 获取Token剩余有效时间
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (Exception e) {
            return 0;
        }
    }
}
