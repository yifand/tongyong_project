package com.vdc.pdi.systemconfig.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统配置模块属性
 */
@Component
@ConfigurationProperties(prefix = "pdi.system-config")
public class SystemConfigProperties {

    /**
     * 是否启用配置缓存
     */
    private Boolean cacheEnabled = true;

    /**
     * 系统配置缓存过期时间（分钟）
     */
    private Integer systemConfigCacheMinutes = 10;

    /**
     * 算法配置缓存过期时间（分钟）
     */
    private Integer algorithmConfigCacheMinutes = 5;

    /**
     * 业务规则缓存过期时间（分钟）
     */
    private Integer businessRuleCacheMinutes = 3;

    /**
     * 配置变更事件是否异步处理
     */
    private Boolean asyncEventHandling = true;

    /**
     * 默认算法配置
     */
    private DefaultAlgorithmConfig defaultAlgorithm = new DefaultAlgorithmConfig();

    /**
     * 初始化配置列表
     */
    private List<InitialConfig> initialConfigs;

    // Getters and Setters
    public Boolean getCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public Integer getSystemConfigCacheMinutes() {
        return systemConfigCacheMinutes;
    }

    public void setSystemConfigCacheMinutes(Integer systemConfigCacheMinutes) {
        this.systemConfigCacheMinutes = systemConfigCacheMinutes;
    }

    public Integer getAlgorithmConfigCacheMinutes() {
        return algorithmConfigCacheMinutes;
    }

    public void setAlgorithmConfigCacheMinutes(Integer algorithmConfigCacheMinutes) {
        this.algorithmConfigCacheMinutes = algorithmConfigCacheMinutes;
    }

    public Integer getBusinessRuleCacheMinutes() {
        return businessRuleCacheMinutes;
    }

    public void setBusinessRuleCacheMinutes(Integer businessRuleCacheMinutes) {
        this.businessRuleCacheMinutes = businessRuleCacheMinutes;
    }

    public Boolean getAsyncEventHandling() {
        return asyncEventHandling;
    }

    public void setAsyncEventHandling(Boolean asyncEventHandling) {
        this.asyncEventHandling = asyncEventHandling;
    }

    public DefaultAlgorithmConfig getDefaultAlgorithm() {
        return defaultAlgorithm;
    }

    public void setDefaultAlgorithm(DefaultAlgorithmConfig defaultAlgorithm) {
        this.defaultAlgorithm = defaultAlgorithm;
    }

    public List<InitialConfig> getInitialConfigs() {
        return initialConfigs;
    }

    public void setInitialConfigs(List<InitialConfig> initialConfigs) {
        this.initialConfigs = initialConfigs;
    }

    /**
     * 默认算法配置
     */
    public static class DefaultAlgorithmConfig {
        /**
         * 默认置信度阈值
         */
        private Integer confidenceThreshold = 80;

        /**
         * 默认检测间隔（毫秒）
         */
        private Integer detectInterval = 1000;

        /**
         * 默认算法版本
         */
        private String version = "1.0.0";

        public Integer getConfidenceThreshold() {
            return confidenceThreshold;
        }

        public void setConfidenceThreshold(Integer confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
        }

        public Integer getDetectInterval() {
            return detectInterval;
        }

        public void setDetectInterval(Integer detectInterval) {
            this.detectInterval = detectInterval;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * 初始化配置项
     */
    public static class InitialConfig {
        private String key;
        private String value;
        private String group;
        private String description;
        private Boolean editable;
        private Boolean builtin;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getEditable() {
            return editable;
        }

        public void setEditable(Boolean editable) {
            this.editable = editable;
        }

        public Boolean getBuiltin() {
            return builtin;
        }

        public void setBuiltin(Boolean builtin) {
            this.builtin = builtin;
        }
    }
}
