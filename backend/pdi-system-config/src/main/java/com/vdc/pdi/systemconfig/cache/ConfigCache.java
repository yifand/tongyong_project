package com.vdc.pdi.systemconfig.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vdc.pdi.systemconfig.domain.entity.SystemConfig;
import com.vdc.pdi.systemconfig.domain.event.ConfigChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 配置缓存
 * 基于Caffeine实现高性能配置读取
 *
 * 缓存键格式（按设计文档）:
 * - algorithm:switch:{channelId}:{algorithmType} - 算法开关缓存
 * - algorithm:config:{channelId} - 算法配置缓存
 * - config:{configKey} - 系统配置缓存
 * - rule:{ruleCode} - 业务规则缓存
 */
@Component
public class ConfigCache {

    /**
     * 系统配置缓存
     * key: config:{configKey}, value: configValue
     */
    private final Cache<String, String> systemConfigCache;

    /**
     * 算法开关缓存
     * key: algorithm:switch:{channelId}:{algorithmType}, value: Boolean
     */
    private final Cache<String, Boolean> algorithmSwitchCache;

    /**
     * 算法配置缓存
     * key: algorithm:config:{channelId}, value: AlgorithmConfig
     */
    private final Cache<String, Object> algorithmConfigCache;

    /**
     * 业务规则缓存
     * key: rule:{ruleCode}, value: BusinessRule
     */
    private final Cache<String, Object> businessRuleCache;

    public ConfigCache() {
        // 系统配置缓存：最大1000条，写入后10分钟过期
        this.systemConfigCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        // 算法开关缓存：最大500条，写入后5分钟过期
        this.algorithmSwitchCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();

        // 算法配置缓存：最大500条，写入后5分钟过期
        this.algorithmConfigCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();

        // 业务规则缓存：最大200条，写入后3分钟过期
        this.businessRuleCache = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    // ========== 缓存键生成方法 ==========

    /**
     * 生成系统配置缓存键
     * format: config:{configKey}
     */
    public String buildSystemConfigKey(String configKey) {
        return "config:" + configKey;
    }

    /**
     * 生成算法开关缓存键
     * format: algorithm:switch:{channelId}:{algorithmType}
     */
    public String buildAlgorithmSwitchKey(Long channelId, String algorithmType) {
        return "algorithm:switch:" + channelId + ":" + algorithmType;
    }

    /**
     * 生成算法配置缓存键
     * format: algorithm:config:{channelId}
     */
    public String buildAlgorithmConfigKey(Long channelId) {
        return "algorithm:config:" + channelId;
    }

    /**
     * 生成业务规则缓存键
     * format: rule:{ruleCode}
     */
    public String buildBusinessRuleKey(String ruleCode) {
        return "rule:" + ruleCode;
    }

    // ========== 系统配置缓存操作 ==========

    /**
     * 获取系统配置值
     */
    public String getSystemConfig(String configKey) {
        String cacheKey = buildSystemConfigKey(configKey);
        return systemConfigCache.getIfPresent(cacheKey);
    }

    /**
     * 设置系统配置缓存
     */
    public void putSystemConfig(String configKey, String configValue) {
        String cacheKey = buildSystemConfigKey(configKey);
        systemConfigCache.put(cacheKey, configValue);
    }

    /**
     * 批量设置系统配置缓存
     */
    public void putAllSystemConfigs(java.util.Map<String, String> configs) {
        java.util.Map<String, String> prefixedConfigs = new java.util.HashMap<>();
        configs.forEach((key, value) -> prefixedConfigs.put(buildSystemConfigKey(key), value));
        systemConfigCache.putAll(prefixedConfigs);
    }

    /**
     * 移除系统配置缓存
     */
    public void removeSystemConfig(String configKey) {
        String cacheKey = buildSystemConfigKey(configKey);
        systemConfigCache.invalidate(cacheKey);
    }

    /**
     * 清空系统配置缓存
     */
    public void clearSystemConfigCache() {
        systemConfigCache.invalidateAll();
    }

    // ========== 算法开关缓存操作 ==========

    /**
     * 获取算法开关状态
     */
    public Boolean getAlgorithmSwitch(Long channelId, String algorithmType) {
        String cacheKey = buildAlgorithmSwitchKey(channelId, algorithmType);
        return algorithmSwitchCache.getIfPresent(cacheKey);
    }

    /**
     * 设置算法开关缓存
     */
    public void putAlgorithmSwitch(Long channelId, String algorithmType, Boolean enabled) {
        String cacheKey = buildAlgorithmSwitchKey(channelId, algorithmType);
        algorithmSwitchCache.put(cacheKey, enabled);
    }

    /**
     * 移除算法开关缓存
     */
    public void removeAlgorithmSwitch(Long channelId, String algorithmType) {
        String cacheKey = buildAlgorithmSwitchKey(channelId, algorithmType);
        algorithmSwitchCache.invalidate(cacheKey);
    }

    /**
     * 根据通道ID移除算法开关缓存
     */
    public void removeAlgorithmSwitchByChannel(Long channelId) {
        String prefix = "algorithm:switch:" + channelId + ":";
        algorithmSwitchCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * 清空算法开关缓存
     */
    public void clearAlgorithmSwitchCache() {
        algorithmSwitchCache.invalidateAll();
    }

    // ========== 算法配置缓存操作 ==========

    /**
     * 获取算法配置
     */
    @SuppressWarnings("unchecked")
    public <T> T getAlgorithmConfig(Long channelId) {
        String cacheKey = buildAlgorithmConfigKey(channelId);
        return (T) algorithmConfigCache.getIfPresent(cacheKey);
    }

    /**
     * 设置算法配置缓存
     */
    public void putAlgorithmConfig(Long channelId, Object config) {
        String cacheKey = buildAlgorithmConfigKey(channelId);
        algorithmConfigCache.put(cacheKey, config);
    }

    /**
     * 移除算法配置缓存
     */
    public void removeAlgorithmConfig(Long channelId) {
        String cacheKey = buildAlgorithmConfigKey(channelId);
        algorithmConfigCache.invalidate(cacheKey);
    }

    /**
     * 根据通道ID移除算法配置缓存
     */
    public void removeAlgorithmConfigByChannel(Long channelId) {
        String prefix = "algorithm:config:" + channelId;
        algorithmConfigCache.asMap().keySet().removeIf(key -> key.equals(prefix) || key.startsWith(prefix + ":"));
    }

    /**
     * 清空算法配置缓存
     */
    public void clearAlgorithmConfigCache() {
        algorithmConfigCache.invalidateAll();
    }

    // ========== 业务规则缓存操作 ==========

    /**
     * 获取业务规则
     */
    @SuppressWarnings("unchecked")
    public <T> T getBusinessRule(String ruleCode) {
        String cacheKey = buildBusinessRuleKey(ruleCode);
        return (T) businessRuleCache.getIfPresent(cacheKey);
    }

    /**
     * 设置业务规则缓存
     */
    public void putBusinessRule(String ruleCode, Object rule) {
        String cacheKey = buildBusinessRuleKey(ruleCode);
        businessRuleCache.put(cacheKey, rule);
    }

    /**
     * 移除业务规则缓存
     */
    public void removeBusinessRule(String ruleCode) {
        String cacheKey = buildBusinessRuleKey(ruleCode);
        businessRuleCache.invalidate(cacheKey);
    }

    /**
     * 清空业务规则缓存
     */
    public void clearBusinessRuleCache() {
        businessRuleCache.invalidateAll();
    }

    // ========== 配置变更事件处理 ==========

    /**
     * 监听配置变更事件，刷新相关缓存
     */
    @EventListener
    public void onConfigChanged(ConfigChangedEvent event) {
        ConfigChangedEvent.ConfigType configType = event.getConfigType();
        String configKey = event.getConfigKey();

        switch (configType) {
            case SYSTEM:
                if (configKey != null) {
                    removeSystemConfig(configKey);
                } else {
                    clearSystemConfigCache();
                }
                break;
            case ALGORITHM:
                if (event.getChannelId() != null) {
                    removeAlgorithmConfigByChannel(event.getChannelId());
                    removeAlgorithmSwitchByChannel(event.getChannelId());
                } else {
                    clearAlgorithmConfigCache();
                    clearAlgorithmSwitchCache();
                }
                break;
            case BUSINESS_RULE:
                if (configKey != null) {
                    removeBusinessRule(configKey);
                } else {
                    clearBusinessRuleCache();
                }
                break;
            default:
                // 未知类型，清空所有缓存
                clearAllCaches();
                break;
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCaches() {
        systemConfigCache.invalidateAll();
        algorithmSwitchCache.invalidateAll();
        algorithmConfigCache.invalidateAll();
        businessRuleCache.invalidateAll();
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        return new CacheStats(
                systemConfigCache.stats().toString(),
                algorithmSwitchCache.stats().toString(),
                algorithmConfigCache.stats().toString(),
                businessRuleCache.stats().toString()
        );
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final String systemConfigStats;
        private final String algorithmSwitchStats;
        private final String algorithmConfigStats;
        private final String businessRuleStats;

        public CacheStats(String systemConfigStats, String algorithmSwitchStats,
                         String algorithmConfigStats, String businessRuleStats) {
            this.systemConfigStats = systemConfigStats;
            this.algorithmSwitchStats = algorithmSwitchStats;
            this.algorithmConfigStats = algorithmConfigStats;
            this.businessRuleStats = businessRuleStats;
        }

        public String getSystemConfigStats() {
            return systemConfigStats;
        }

        public String getAlgorithmSwitchStats() {
            return algorithmSwitchStats;
        }

        public String getAlgorithmConfigStats() {
            return algorithmConfigStats;
        }

        public String getBusinessRuleStats() {
            return businessRuleStats;
        }
    }
}
