package com.vdc.platform.ruleengine.config;

import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.service.IRuleConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleConfigCache {

    private final IRuleConfigService ruleConfigService;
    private final ConcurrentHashMap<String, RuleConfig> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedRate = 60000)
    public void refresh() {
        List<RuleConfig> configs = ruleConfigService.lambdaQuery()
                .eq(RuleConfig::getIsEnabled, true)
                .list();

        ConcurrentHashMap<String, RuleConfig> newCache = new ConcurrentHashMap<>();
        for (RuleConfig config : configs) {
            if (config.getChannelType() != null) {
                newCache.put(config.getChannelType(), config);
            }
        }

        cache.clear();
        cache.putAll(newCache);
        log.debug("RuleConfigCache refreshed, size={}", cache.size());
    }

    public RuleConfig get(String channelType) {
        RuleConfig config = cache.get(channelType);
        if (config == null) {
            config = ruleConfigService.lambdaQuery()
                    .eq(RuleConfig::getChannelType, channelType)
                    .eq(RuleConfig::getIsEnabled, true)
                    .last("LIMIT 1")
                    .one();
            if (config != null) {
                cache.put(channelType, config);
            }
        }
        return config;
    }

    public RuleConfig getOrDefault(String channelType) {
        RuleConfig config = get(channelType);
        if (config == null) {
            config = createDefaultRuleConfig();
        }
        return config;
    }

    private RuleConfig createDefaultRuleConfig() {
        RuleConfig defaultConfig = new RuleConfig();
        defaultConfig.setRuleName("DEFAULT");
        defaultConfig.setChannelType("DEFAULT");
        defaultConfig.setRequireVehicle(true);
        defaultConfig.setEnterPattern(List.of(15, 16, 11));
        defaultConfig.setExitPattern(List.of(11, 16, 15, List.of(13, 9)));
        defaultConfig.setStandardDuration(300);
        defaultConfig.setCriticalThresholdPct(new java.math.BigDecimal("90.00"));
        defaultConfig.setIsEnabled(true);
        return defaultConfig;
    }
}
