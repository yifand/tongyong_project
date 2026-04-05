package com.vdc.pdi.algorithminlet.validator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 盒子认证器
 * 负责验证边缘盒子的Token合法性
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BoxAuthenticator {

    private final EdgeBoxRepository edgeBoxRepository;

    @Value("${algorithm-inlet.auth.cache-expire-minutes:30}")
    private int cacheExpireMinutes;

    @Value("${algorithm-inlet.auth.secret-key:default-secret-key}")
    private String secretKey;

    private Cache<String, BoxAuthResult> authCache;

    @PostConstruct
    public void init() {
        authCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(cacheExpireMinutes))
                .build();
    }

    /**
     * 盒子认证
     *
     * @param token 请求Token
     * @param boxId 盒子ID
     * @return 认证结果
     */
    public BoxAuthResult authenticate(String token, String boxId) {
        if (token == null || token.isEmpty()) {
            return BoxAuthResult.invalid("Token不能为空");
        }
        if (boxId == null || boxId.isEmpty()) {
            return BoxAuthResult.invalid("盒子ID不能为空");
        }

        // 缓存检查
        String cacheKey = token + ":" + boxId;
        BoxAuthResult cached = authCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("盒子认证缓存命中: boxId={}", boxId);
            return cached;
        }

        // 查找盒子
        Optional<EdgeBox> boxOpt = findBoxByCode(boxId);
        if (boxOpt.isEmpty()) {
            log.warn("盒子不存在: boxId={}", boxId);
            return BoxAuthResult.invalid("盒子不存在");
        }

        EdgeBox box = boxOpt.get();

        // Token验证 (使用HMAC-SHA256)
        String expectedToken = generateToken(boxId);
        if (!expectedToken.equals(token)) {
            log.warn("Token验证失败: boxId={}", boxId);
            return BoxAuthResult.invalid("Token无效");
        }

        BoxAuthResult result = BoxAuthResult.valid(box.getSiteId(), box.getId());
        authCache.put(cacheKey, result);
        log.debug("盒子认证成功: boxId={}, siteId={}", boxId, box.getSiteId());

        return result;
    }

    /**
     * 根据盒子编码查找盒子
     * 由于EdgeBox没有boxId字段，这里使用ID作为编码
     */
    private Optional<EdgeBox> findBoxByCode(String boxId) {
        try {
            Long id = Long.parseLong(boxId.replaceAll("[^0-9]", ""));
            return edgeBoxRepository.findByIdAndDeletedAtIsNull(id);
        } catch (NumberFormatException e) {
            // 尝试直接查找
            return edgeBoxRepository.findAllByDeletedAtIsNull().stream()
                    .filter(box -> boxId.equals(String.valueOf(box.getId())))
                    .findFirst();
        }
    }

    /**
     * 生成Token
     * 使用HMAC-SHA256算法
     */
    private String generateToken(String boxId) {
        String data = boxId + ":" + LocalDate.now().toString();
        return HmacUtils.hmacSha256Hex(secretKey, data);
    }

    /**
     * 清除认证缓存
     */
    public void clearCache() {
        authCache.invalidateAll();
    }
}
