package com.vdc.pdi.systemconfig.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 系统配置实体
 * 存储通用系统配置项
 */
@Entity
@Table(name = "system_config", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key"),
    @Index(name = "idx_config_group", columnList = "config_group"),
    @Index(name = "idx_site_id", columnList = "site_id")
})
public class SystemConfig extends BaseEntity {

    /**
     * 配置键
     */
    @Column(name = "config_key", nullable = false, length = 128)
    private String configKey;

    /**
     * 配置值
     */
    @Column(name = "config_value", nullable = false, length = 2048)
    private String configValue;

    /**
     * 配置分组
     */
    @Column(name = "config_group", nullable = false, length = 64)
    private String configGroup;

    /**
     * 配置描述
     */
    @Column(name = "description", length = 512)
    private String description;

    /**
     * 是否可编辑
     */
    @Column(name = "editable", nullable = false)
    private Boolean editable = true;

    /**
     * 是否系统内置
     */
    @Column(name = "builtin", nullable = false)
    private Boolean builtin = false;

    /**
     * 配置数据类型
     */
    @Column(name = "value_type", length = 32)
    @Enumerated(EnumType.STRING)
    private ValueType valueType = ValueType.STRING;

    /**
     * 默认值
     */
    @Column(name = "default_value", length = 2048)
    private String defaultValue;

    /**
     * 配置排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 值类型枚举
     */
    public enum ValueType {
        STRING,
        INTEGER,
        LONG,
        DOUBLE,
        BOOLEAN,
        JSON,
        ARRAY
    }

    // Getters and Setters
    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
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

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
