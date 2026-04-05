package com.vdc.pdi.systemconfig.domain.entity;

import com.vdc.pdi.common.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 业务规则实体
 * 存储业务规则配置
 */
@Entity
@Table(name = "business_rule", indexes = {
    @Index(name = "idx_rule_code", columnList = "rule_code"),
    @Index(name = "idx_rule_type", columnList = "rule_type"),
    @Index(name = "idx_rule_enabled", columnList = "enabled"),
    @Index(name = "idx_rule_site", columnList = "site_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_rule_code_site", columnNames = {"rule_code", "site_id"})
})
public class BusinessRule extends BaseEntity {

    /**
     * 规则编码（唯一标识）
     */
    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    /**
     * 规则类型: STATE_TRANSITION-状态转换, PDI_STANDARD_TIME-PDI标准工时, ALARM_THRESHOLD-报警阈值
     */
    @Column(name = "rule_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    /**
     * 规则配置（JSON格式）
     */
    @Column(name = "rule_config", nullable = false, columnDefinition = "TEXT")
    private String ruleConfig;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 规则描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Column(name = "priority")
    private Integer priority = 0;

    /**
     * 规则类型枚举
     */
    public enum RuleType {
        STATE_TRANSITION,      // 状态转换规则
        PDI_STANDARD_TIME,     // PDI标准工时规则
        ALARM_THRESHOLD        // 报警阈值规则
    }

    // Getters and Setters
    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
