package com.vdc.platform.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtil jwtUtil;

    public void storeRefreshToken(String username, String refreshToken) {
        String jti = jwtUtil.getJti(refreshToken);
        String key = buildRefreshKey(username, jti);
        long ttl = jwtUtil.getRemainingTimeMillis(refreshToken);
        if (ttl > 0) {
            stringRedisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean validateRefreshToken(String username, String refreshToken) {
        String jti = jwtUtil.getJti(refreshToken);
        String key = buildRefreshKey(username, jti);
        String stored = stringRedisTemplate.opsForValue().get(key);
        return refreshToken.equals(stored);
    }

    public void deleteRefreshToken(String username, String refreshToken) {
        String jti = jwtUtil.getJti(refreshToken);
        String key = buildRefreshKey(username, jti);
        stringRedisTemplate.delete(key);
    }

    public void blacklistAccessToken(String accessToken) {
        String jti = jwtUtil.getJti(accessToken);
        long ttl = jwtUtil.getRemainingTimeMillis(accessToken);
        if (ttl > 0) {
            String key = "blacklist:" + jti;
            stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        String jti = jwtUtil.getJti(accessToken);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey("blacklist:" + jti));
    }

    private String buildRefreshKey(String username, String jti) {
        return "refresh:" + username + ":" + jti;
    }
}
