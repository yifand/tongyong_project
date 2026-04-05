package com.vdc.pdi.systemconfig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.systemconfig.cache.ConfigCache;
import com.vdc.pdi.systemconfig.domain.entity.AlgorithmConfig;
import com.vdc.pdi.systemconfig.domain.event.ConfigChangedEvent;
import com.vdc.pdi.systemconfig.domain.repository.AlgorithmConfigRepository;
import com.vdc.pdi.systemconfig.dto.request.AlgorithmConfigRequest;
import com.vdc.pdi.systemconfig.dto.response.AlgorithmConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 算法配置服务实现
 */
@Service
@Transactional(readOnly = true)
public class AlgorithmConfigServiceImpl implements AlgorithmConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AlgorithmConfigServiceImpl.class);

    private final AlgorithmConfigRepository algorithmConfigRepository;
    private final ObjectMapper objectMapper;
    private final ConfigCache configCache;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public AlgorithmConfigServiceImpl(AlgorithmConfigRepository algorithmConfigRepository,
                                      ObjectMapper objectMapper,
                                      ConfigCache configCache,
                                      ApplicationEventPublisher eventPublisher) {
        this.algorithmConfigRepository = algorithmConfigRepository;
        this.objectMapper = objectMapper;
        this.configCache = configCache;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AlgorithmConfigResponse getConfig(Long channelId, String algorithmType) {
        // 默认站点ID为0
        Long siteId = 0L;

        // 优先从缓存读取通道配置
        if (channelId != null) {
            @SuppressWarnings("unchecked")
            Map<String, AlgorithmConfigResponse> cachedMap = configCache.getAlgorithmConfig(channelId);
            if (cachedMap != null && cachedMap.containsKey(algorithmType)) {
                return cachedMap.get(algorithmType);
            }
        }

        AlgorithmConfig config;
        AlgorithmConfigResponse response;
        if (channelId != null) {
            // 优先查询通道配置
            config = algorithmConfigRepository
                    .findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(siteId, channelId, algorithmType)
                    .orElse(null);

            if (config == null || Boolean.TRUE.equals(config.getInheritGlobal())) {
                // 继承全局配置或不存在，查询全局配置
                AlgorithmConfig globalConfig = algorithmConfigRepository
                        .findGlobalConfigByAlgorithmType(siteId, algorithmType)
                        .orElse(null);

                if (config != null && globalConfig != null) {
                    // 合并配置：通道配置优先，未设置的字段使用全局配置
                    response = mergeConfig(config, globalConfig);
                } else if (config != null) {
                    response = toResponse(config);
                } else if (globalConfig != null) {
                    response = toResponse(globalConfig);
                    response.setInheritGlobal(true); // 继承全局配置，标记为继承
                } else {
                    response = createDefaultConfig(algorithmType);
                }
            } else {
                response = toResponse(config);
            }

            // 写入缓存
            @SuppressWarnings("unchecked")
            Map<String, AlgorithmConfigResponse> cachedMap = configCache.getAlgorithmConfig(channelId);
            Map<String, AlgorithmConfigResponse> map = cachedMap != null ? cachedMap : new HashMap<>();
            map.put(algorithmType, response);
            configCache.putAlgorithmConfig(channelId, map);
        } else {
            // 查询全局配置
            config = algorithmConfigRepository
                    .findGlobalConfigByAlgorithmType(siteId, algorithmType)
                    .orElse(null);
            if (config != null) {
                response = toResponse(config);
            } else {
                response = createDefaultConfig(algorithmType);
            }
        }

        return response;
    }

    @Override
    @Transactional
    public void updateConfig(Long channelId, AlgorithmConfigRequest request) {
        Long siteId = 0L;
        String algorithmType = request.getAlgorithmType();

        AlgorithmConfig config = algorithmConfigRepository
                .findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(siteId, channelId, algorithmType)
                .orElseGet(() -> {
                    AlgorithmConfig newConfig = new AlgorithmConfig();
                    newConfig.setSiteId(siteId);
                    newConfig.setChannelId(channelId);
                    newConfig.setAlgorithmType(algorithmType);
                    newConfig.setCreatedAt(LocalDateTime.now());
                    return newConfig;
                });

        updateConfigFromRequest(config, request);
        config.setUpdatedAt(LocalDateTime.now());
        algorithmConfigRepository.save(config);

        eventPublisher.publishEvent(ConfigChangedEvent.algorithmConfigChanged(
                this, config.getId(), channelId, ConfigChangedEvent.OperationType.UPDATE));

        logger.info("更新通道算法配置: siteId={}, channelId={}, algorithmType={}", siteId, channelId, algorithmType);
    }

    @Override
    public AlgorithmConfigResponse getGlobalConfig(String algorithmType) {
        Long siteId = 0L;
        return algorithmConfigRepository
                .findGlobalConfigByAlgorithmType(siteId, algorithmType)
                .map(this::toResponse)
                .orElseGet(() -> createDefaultConfig(algorithmType));
    }

    @Override
    @Transactional
    public void updateGlobalConfig(AlgorithmConfigRequest request) {
        Long siteId = 0L;
        String algorithmType = request.getAlgorithmType();

        AlgorithmConfig config = algorithmConfigRepository
                .findGlobalConfigByAlgorithmType(siteId, algorithmType)
                .orElseGet(() -> {
                    AlgorithmConfig newConfig = new AlgorithmConfig();
                    newConfig.setSiteId(siteId);
                    newConfig.setChannelId(null);
                    newConfig.setAlgorithmType(algorithmType);
                    newConfig.setCreatedAt(LocalDateTime.now());
                    return newConfig;
                });

        updateConfigFromRequest(config, request);
        config.setUpdatedAt(LocalDateTime.now());
        algorithmConfigRepository.save(config);

        eventPublisher.publishEvent(ConfigChangedEvent.algorithmConfigChanged(
                this, config.getId(), null, ConfigChangedEvent.OperationType.UPDATE));

        logger.info("更新全局算法配置: siteId={}, algorithmType={}", siteId, algorithmType);
    }

    @Override
    public PageResult<AlgorithmConfigResponse> listChannelConfigs(Long channelId, int page, int size) {
        Long siteId = 0L;
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<AlgorithmConfig> configPage;
        if (channelId != null) {
            configPage = algorithmConfigRepository.findBySiteIdAndChannelIdAndDeletedAtIsNull(siteId, channelId, pageable);
        } else {
            // 查询全局配置
            configPage = algorithmConfigRepository.findBySiteIdAndChannelIdIsNullAndDeletedAtIsNull(siteId, pageable);
        }

        return PageResult.of(
                configPage.getContent().stream().map(this::toResponse).toList(),
                configPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public boolean isAlgorithmEnabled(Long channelId, String algorithmType) {
        Boolean cached = configCache.getAlgorithmSwitch(channelId, algorithmType);
        if (cached != null) {
            return cached;
        }
        AlgorithmConfigResponse config = getConfig(channelId, algorithmType);
        boolean enabled = Boolean.TRUE.equals(config.getEnabled());
        configCache.putAlgorithmSwitch(channelId, algorithmType, enabled);
        return enabled;
    }

    @Override
    public Integer getStandardDuration(Long channelId) {
        AlgorithmConfigResponse config = getConfig(channelId, "PDI_LEFT_FRONT");
        return config.getStandardDuration();
    }

    @Override
    public Integer getPersonDisappearTimeout(Long channelId) {
        AlgorithmConfigResponse config = getConfig(channelId, "PDI_LEFT_FRONT");
        return config.getPersonDisappearTimeout() != null ?
                config.getPersonDisappearTimeout() : 10;
    }

    @Override
    public Integer getEnterExitWindow(Long channelId) {
        AlgorithmConfigResponse config = getConfig(channelId, "PDI_LEFT_FRONT");
        return config.getEnterExitWindow() != null ?
                config.getEnterExitWindow() : 5;
    }

    /**
     * 合并通道配置和全局配置
     */
    private AlgorithmConfigResponse mergeConfig(AlgorithmConfig channelConfig, AlgorithmConfig globalConfig) {
        AlgorithmConfigResponse response = new AlgorithmConfigResponse();

        response.setId(channelConfig.getId());
        response.setChannelId(channelConfig.getChannelId());
        response.setAlgorithmType(channelConfig.getAlgorithmType());

        // 通道配置优先，未设置的字段使用全局配置
        response.setEnabled(channelConfig.getEnabled() != null ?
                channelConfig.getEnabled() : globalConfig.getEnabled());
        response.setSensitivity(channelConfig.getSensitivity() != null ?
                channelConfig.getSensitivity() : globalConfig.getSensitivity());
        response.setTriggerFrames(channelConfig.getTriggerFrames() != null ?
                channelConfig.getTriggerFrames() : globalConfig.getTriggerFrames());
        response.setStandardDuration(channelConfig.getStandardDuration() != null ?
                channelConfig.getStandardDuration() : globalConfig.getStandardDuration());
        response.setEnterExitWindow(channelConfig.getEnterExitWindow() != null ?
                channelConfig.getEnterExitWindow() : globalConfig.getEnterExitWindow());
        response.setPersonDisappearTimeout(channelConfig.getPersonDisappearTimeout() != null ?
                channelConfig.getPersonDisappearTimeout() : globalConfig.getPersonDisappearTimeout());
        response.setExtraParams(channelConfig.getExtraParams() != null ?
                channelConfig.getExtraParams() : globalConfig.getExtraParams());

        response.setCreatedAt(channelConfig.getCreatedAt());
        response.setUpdatedAt(channelConfig.getUpdatedAt());

        return response;
    }

    /**
     * 从请求更新配置实体
     */
    private void updateConfigFromRequest(AlgorithmConfig config, AlgorithmConfigRequest request) {
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        }
        if (request.getSensitivity() != null) {
            config.setSensitivity(request.getSensitivity());
        }
        if (request.getTriggerFrames() != null) {
            config.setTriggerFrames(request.getTriggerFrames());
        }
        if (request.getStandardDuration() != null) {
            config.setStandardDuration(request.getStandardDuration());
        }
        if (request.getEnterExitWindow() != null) {
            config.setEnterExitWindow(request.getEnterExitWindow());
        }
        if (request.getPersonDisappearTimeout() != null) {
            config.setPersonDisappearTimeout(request.getPersonDisappearTimeout());
        }
        if (request.getInheritGlobal() != null) {
            config.setInheritGlobal(request.getInheritGlobal());
        }
        if (request.getExtraParams() != null) {
            config.setExtraParams(request.getExtraParams());
        }
    }

    /**
     * 转换为响应DTO
     */
    private AlgorithmConfigResponse toResponse(AlgorithmConfig config) {
        AlgorithmConfigResponse response = new AlgorithmConfigResponse();
        response.setId(config.getId());
        response.setChannelId(config.getChannelId());
        response.setAlgorithmType(config.getAlgorithmType());
        response.setEnabled(config.getEnabled());
        response.setSensitivity(config.getSensitivity());
        response.setTriggerFrames(config.getTriggerFrames());
        response.setStandardDuration(config.getStandardDuration());
        response.setEnterExitWindow(config.getEnterExitWindow());
        response.setPersonDisappearTimeout(config.getPersonDisappearTimeout());
        response.setInheritGlobal(config.getInheritGlobal());
        response.setExtraParams(config.getExtraParams());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }

    /**
     * 创建默认配置
     */
    private AlgorithmConfigResponse createDefaultConfig(String algorithmType) {
        AlgorithmConfigResponse response = new AlgorithmConfigResponse();
        response.setAlgorithmType(algorithmType);
        response.setEnabled(true);
        response.setSensitivity("MEDIUM");
        response.setTriggerFrames(3);

        // PDI算法特有字段
        if (algorithmType != null && algorithmType.startsWith("PDI_")) {
            response.setStandardDuration(300);
            response.setEnterExitWindow(5);
            response.setPersonDisappearTimeout(10);
        }

        response.setInheritGlobal(true);
        return response;
    }
}
