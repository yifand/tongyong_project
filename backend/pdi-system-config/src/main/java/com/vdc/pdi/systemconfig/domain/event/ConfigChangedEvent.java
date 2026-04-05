package com.vdc.pdi.systemconfig.domain.event;

import org.springframework.context.ApplicationEvent;

/**
 * 配置变更领域事件
 * 当系统配置、算法配置或业务规则发生变更时触发
 */
public class ConfigChangedEvent extends ApplicationEvent {

    /**
     * 配置类型
     */
    private final ConfigType configType;

    /**
     * 配置键（系统配置使用）
     */
    private final String configKey;

    /**
     * 配置ID
     */
    private final Long configId;

    /**
     * 通道ID（算法配置使用）
     */
    private final Long channelId;

    /**
     * 变更操作类型
     */
    private final OperationType operationType;

    /**
     * 变更时间戳
     */
    private final long timestamp;

    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        SYSTEM,         // 系统配置
        ALGORITHM,      // 算法配置
        BUSINESS_RULE   // 业务规则
    }

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        CREATE,     // 创建
        UPDATE,     // 更新
        DELETE,     // 删除
        ENABLE,     // 启用
        DISABLE     // 禁用
    }

    /**
     * 创建配置变更事件
     */
    public ConfigChangedEvent(Object source, ConfigType configType, OperationType operationType) {
        super(source);
        this.configType = configType;
        this.operationType = operationType;
        this.configKey = null;
        this.configId = null;
        this.channelId = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建配置变更事件（带配置键）
     */
    public ConfigChangedEvent(Object source, ConfigType configType, String configKey, OperationType operationType) {
        super(source);
        this.configType = configType;
        this.configKey = configKey;
        this.operationType = operationType;
        this.configId = null;
        this.channelId = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建配置变更事件（带配置ID）
     */
    public ConfigChangedEvent(Object source, ConfigType configType, Long configId, OperationType operationType) {
        super(source);
        this.configType = configType;
        this.configId = configId;
        this.operationType = operationType;
        this.configKey = null;
        this.channelId = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建配置变更事件（带通道ID）
     */
    public ConfigChangedEvent(Object source, ConfigType configType, Long configId, Long channelId, OperationType operationType) {
        super(source);
        this.configType = configType;
        this.configId = configId;
        this.channelId = channelId;
        this.operationType = operationType;
        this.configKey = null;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public ConfigType getConfigType() {
        return configType;
    }

    public String getConfigKey() {
        return configKey;
    }

    public Long getConfigId() {
        return configId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public long getEventTimestamp() {
        return timestamp;
    }

    /**
     * 创建系统配置变更事件
     */
    public static ConfigChangedEvent systemConfigChanged(Object source, String configKey, OperationType operationType) {
        return new ConfigChangedEvent(source, ConfigType.SYSTEM, configKey, operationType);
    }

    /**
     * 创建算法配置变更事件
     */
    public static ConfigChangedEvent algorithmConfigChanged(Object source, Long configId, Long channelId, OperationType operationType) {
        return new ConfigChangedEvent(source, ConfigType.ALGORITHM, configId, channelId, operationType);
    }

    /**
     * 创建业务规则变更事件
     */
    public static ConfigChangedEvent businessRuleChanged(Object source, Long ruleId, OperationType operationType) {
        return new ConfigChangedEvent(source, ConfigType.BUSINESS_RULE, ruleId, operationType);
    }

    @Override
    public String toString() {
        return "ConfigChangedEvent{" +
                "configType=" + configType +
                ", configKey='" + configKey + '\'' +
                ", configId=" + configId +
                ", channelId=" + channelId +
                ", operationType=" + operationType +
                ", timestamp=" + timestamp +
                '}';
    }
}
