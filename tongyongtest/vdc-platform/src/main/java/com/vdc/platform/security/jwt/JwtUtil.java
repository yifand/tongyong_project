package com.vdc.platform.security.jwt;

import com.vdc.platform.security.model.SecurityUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    @Value("${vdc.jwt.secret:dmRjLXBsYXRmb3JtLWp3dC1zZWNyZXQta2V5LTI1Ni1iaXQ=}")
    private String secret;

    @Value("${vdc.jwt.access-token-expiration:7200000}")
    private long accessTokenExpiration;

    @Value("${vdc.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("permissions", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        if (userDetails instanceof SecurityUser su) {
            claims.put("userId", su.getUserId());
            claims.put("siteId", su.getSiteId());
            claims.put("roleCode", su.getRoleCode());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .id(UUID.randomUUID().toString())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .id(UUID.randomUUID().toString())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public Date getExpirationDate(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration();
    }

    public String getJti(String token) {
        Claims claims = parseClaims(token);
        return claims.getId();
    }

    public long getRemainingTimeMillis(String token) {
        Date expiration = getExpirationDate(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
