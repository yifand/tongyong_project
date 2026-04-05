package com.vdc.pdi.systemconfig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.systemconfig.cache.ConfigCache;
import com.vdc.pdi.systemconfig.domain.entity.SystemConfig;
import com.vdc.pdi.systemconfig.domain.event.ConfigChangedEvent;
import com.vdc.pdi.systemconfig.domain.repository.SystemConfigRepository;
import com.vdc.pdi.systemconfig.dto.request.GeneralConfigRequest;
import com.vdc.pdi.systemconfig.dto.response.ConfigGroupResponse;
import com.vdc.pdi.systemconfig.dto.response.ConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 */
@Service
@Transactional(readOnly = true)
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    private final SystemConfigRepository configRepository;
    private final ObjectMapper objectMapper;
    private final ConfigCache configCache;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public SystemConfigServiceImpl(SystemConfigRepository configRepository,
                                    ObjectMapper objectMapper,
                                    ConfigCache configCache,
                                    ApplicationEventPublisher eventPublisher) {
        this.configRepository = configRepository;
        this.objectMapper = objectMapper;
        this.configCache = configCache;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ConfigGroupResponse getGeneralConfig(String configGroup) {
        Long siteId = 0L;

        List<SystemConfig> configs = configRepository
                .findByConfigGroupAndDeletedAtIsNullOrderBySortOrderAsc(configGroup);

        // 过滤站点配置
        List<SystemConfig> siteConfigs = configs.stream()
                .filter(c -> siteId.equals(c.getSiteId()))
                .toList();

        if (siteConfigs.isEmpty()) {
            // 尝试使用默认站点配置
            siteConfigs = configs.stream()
                    .filter(c -> c.getSiteId() == null || c.getSiteId() == 0L)
                    .toList();
        }

        return toGroupResponse(configGroup, siteConfigs);
    }

    @Override
    @Transactional
    public void updateGeneralConfig(GeneralConfigRequest request) {
        Long siteId = 0L;
        String configGroup = request.getConfigGroup();

        for (GeneralConfigRequest.ConfigItem item : request.getConfigs()) {
            SystemConfig config = configRepository
                    .findByConfigKeyAndDeletedAtIsNull(item.getConfigKey())
                    .orElse(null);

            if (config == null) {
                // 创建新配置
                config = new SystemConfig();
                config.setSiteId(siteId);
                config.setConfigKey(item.getConfigKey());
                config.setConfigGroup(configGroup);
                config.setCreatedAt(LocalDateTime.now());
            }

            // 检查是否可编辑
            if (Boolean.FALSE.equals(config.getEditable())) {
                logger.warn("尝试修改不可编辑的配置: {}", item.getConfigKey());
                continue;
            }

            config.setConfigValue(item.getConfigValue());
            config.setUpdatedAt(LocalDateTime.now());
            configRepository.save(config);

            // 发布配置变更事件
            eventPublisher.publishEvent(ConfigChangedEvent.systemConfigChanged(
                    this, item.getConfigKey(), ConfigChangedEvent.OperationType.UPDATE));
        }

        logger.info("批量更新通用配置: configGroup={}, count={}", configGroup, request.getConfigs().size());
    }

    @Override
    public ConfigResponse getConfigByKey(String configKey) {
        Long siteId = 0L;

        // 先查询站点特定配置
        SystemConfig config = configRepository
                .findBySiteIdAndConfigKey(siteId, configKey)
                .filter(c -> c.getDeletedAt() == null)
                .orElseGet(() -> configRepository
                        .findByConfigKeyAndDeletedAtIsNull(configKey)
                        .orElseThrow(() -> new BusinessException("配置不存在: " + configKey)));

        return toResponse(config);
    }

    @Override
    public String getStringValue(String configKey, String defaultValue) {
        String cached = configCache.getSystemConfig(configKey);
        if (cached != null) {
            return cached;
        }
        String value = configRepository.findByConfigKeyAndDeletedAtIsNull(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
        if (value != null) {
            configCache.putSystemConfig(configKey, value);
            return value;
        }
        return defaultValue;
    }

    @Override
    public Integer getIntValue(String configKey, Integer defaultValue) {
        String value = getStringValue(configKey, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("配置值无法转换为整数: configKey={}, value={}", configKey, value);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanValue(String configKey, Boolean defaultValue) {
        String value = getStringValue(configKey, null);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public <T> T getJsonValue(String configKey, Class<T> clazz) {
        String value = getStringValue(configKey, null);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            logger.error("配置值JSON解析失败: configKey={}, error={}", configKey, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T getJsonValue(String configKey, Class<T> clazz, T defaultValue) {
        T value = getJsonValue(configKey, clazz);
        return value != null ? value : defaultValue;
    }

    @Override
    public List<String> listConfigGroups() {
        return configRepository.findAllByDeletedAtIsNullOrderByConfigGroupAscSortOrderAsc()
                .stream()
                .map(SystemConfig::getConfigGroup)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应DTO
     */
    private ConfigResponse toResponse(SystemConfig config) {
        ConfigResponse response = new ConfigResponse();
        response.setId(config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setConfigGroup(config.getConfigGroup());
        response.setDescription(config.getDescription());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }

    /**
     * 转换为分组响应DTO
     */
    private ConfigGroupResponse toGroupResponse(String group, List<SystemConfig> configs) {
        ConfigGroupResponse response = new ConfigGroupResponse();
        response.setConfigGroup(group);
        response.setDescription(getGroupDescription(group));
        response.setConfigs(configs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * 获取分组描述
     */
    private String getGroupDescription(String group) {
        return switch (group) {
            case "system" -> "系统基础配置";
            case "alarm" -> "告警相关配置";
            case "algorithm" -> "算法相关配置";
            case "notification" -> "通知相关配置";
            case "storage" -> "存储相关配置";
            default -> "通用配置";
        };
    }
}
