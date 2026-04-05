package com.vdc.pdi.auth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * 使用Caffeine缓存实现，用于存储已登出的Token
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    /**
     * Token黑名单缓存
     * 过期时间设置为7天，与刷新Token过期时间一致
     */
    private final Cache<String, Boolean> blacklistCache;

    public TokenBlacklistService() {
        this.blacklistCache = Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS)
                .maximumSize(10000)
                .recordStats()
                .build();
    }

    /**
     * 将Token加入黑名单
     */
    public void blacklistToken(String token, long expirationTime) {
        blacklistCache.put(token, true);
        logger.debug("Token added to blacklist, expires in {} ms", expirationTime);
    }

    /**
     * 检查Token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        Boolean blacklisted = blacklistCache.getIfPresent(token);
        return blacklisted != null && blacklisted;
    }

    /**
     * 从黑名单中移除Token（通常不需要手动调用，由缓存过期自动处理）
     */
    public void removeFromBlacklist(String token) {
        blacklistCache.invalidate(token);
    }

    /**
     * 清空黑名单
     */
    public void clearBlacklist() {
        blacklistCache.invalidateAll();
        logger.info("Token blacklist cleared");
    }

    /**
     * 获取黑名单统计信息
     */
    public long getBlacklistSize() {
        return blacklistCache.estimatedSize();
    }
}
