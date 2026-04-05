package com.vdc.pdi.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录锁定服务
 * 使用Caffeine缓存实现登录失败次数记录和账户锁定
 */
@Service
public class LoginLockService {

    private static final Logger logger = LoggerFactory.getLogger(LoginLockService.class);

    @Value("${auth.login.max-fail-attempts:5}")
    private int maxFailAttempts;

    @Value("${auth.login.lock-duration:30}")
    private int lockDurationMinutes;

    /**
     * 登录失败次数缓存
     */
    private final Cache<String, AtomicInteger> failCountCache;

    /**
     * 账户锁定缓存
     */
    private final Cache<String, Boolean> lockCache;

    public LoginLockService() {
        this.failCountCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        this.lockCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }

    /**
     * 记录登录失败
     */
    public void recordFailedAttempt(String username) {
        AtomicInteger count = failCountCache.get(username, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();

        logger.debug("Login failed attempt {} for user: {}", currentCount, username);

        // 超过最大失败次数，锁定账户
        if (currentCount >= maxFailAttempts) {
            lock(username);
            logger.warn("Account locked due to too many failed attempts: {}", username);
        }
    }

    /**
     * 获取登录失败次数
     */
    public int getFailedAttempts(String username) {
        AtomicInteger count = failCountCache.getIfPresent(username);
        return count != null ? count.get() : 0;
    }

    /**
     * 锁定账户
     */
    public void lock(String username) {
        lockCache.put(username, true);
    }

    /**
     * 解锁账户
     */
    public void unlock(String username) {
        lockCache.invalidate(username);
        failCountCache.invalidate(username);
        logger.debug("Account unlocked: {}", username);
    }

    /**
     * 检查账户是否被锁定
     */
    public boolean isLocked(String username) {
        Boolean locked = lockCache.getIfPresent(username);
        return locked != null && locked;
    }

    /**
     * 获取剩余锁定时间（分钟）
     */
    public long getRemainingLockTime(String username) {
        if (!isLocked(username)) {
            return 0;
        }
        return lockDurationMinutes;
    }

    /**
     * 清除用户的登录失败记录
     */
    public void clearFailedAttempts(String username) {
        failCountCache.invalidate(username);
    }
}
