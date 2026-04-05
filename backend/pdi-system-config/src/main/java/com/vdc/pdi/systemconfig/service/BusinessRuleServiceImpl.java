package com.vdc.pdi.systemconfig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.systemconfig.cache.ConfigCache;
import com.vdc.pdi.systemconfig.domain.entity.BusinessRule;
import com.vdc.pdi.systemconfig.domain.event.ConfigChangedEvent;
import com.vdc.pdi.systemconfig.domain.repository.BusinessRuleRepository;
import com.vdc.pdi.systemconfig.dto.request.BusinessRuleRequest;
import com.vdc.pdi.systemconfig.dto.response.BusinessRuleResponse;
import com.vdc.pdi.systemconfig.dto.rule.AlarmThresholdConfig;
import com.vdc.pdi.systemconfig.dto.rule.PdiStandardTimeConfig;
import com.vdc.pdi.systemconfig.dto.rule.StateTransitionRule;
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
import java.util.List;

/**
 * 业务规则服务实现
 */
@Service
@Transactional(readOnly = true)
public class BusinessRuleServiceImpl implements BusinessRuleService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleServiceImpl.class);

    private final BusinessRuleRepository businessRuleRepository;
    private final ObjectMapper objectMapper;
    private final ConfigCache configCache;
    private final ApplicationEventPublisher eventPublisher;

    // 规则类型常量
    private static final String RULE_TYPE_STATE_TRANSITION = "STATE_TRANSITION";
    private static final String RULE_TYPE_PDI_STANDARD_TIME = "PDI_STANDARD_TIME";
    private static final String RULE_TYPE_ALARM_THRESHOLD = "ALARM_THRESHOLD";

    @Autowired
    public BusinessRuleServiceImpl(BusinessRuleRepository businessRuleRepository,
                                    ObjectMapper objectMapper,
                                    ConfigCache configCache,
                                    ApplicationEventPublisher eventPublisher) {
        this.businessRuleRepository = businessRuleRepository;
        this.objectMapper = objectMapper;
        this.configCache = configCache;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PageResult<BusinessRuleResponse> listRules(int page, int size, String ruleType) {
        Long siteId = 0L;
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<BusinessRule> rulePage;
        if (ruleType != null && !ruleType.isEmpty()) {
            rulePage = businessRuleRepository.findBySiteIdAndRuleTypeAndDeletedAtIsNull(siteId, ruleType, pageable);
        } else {
            rulePage = businessRuleRepository.findBySiteIdAndDeletedAtIsNull(siteId, pageable);
        }

        return PageResult.of(
                rulePage.getContent().stream().map(this::toResponse).toList(),
                rulePage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public BusinessRuleResponse getRule(Long id) {
        BusinessRule rule = businessRuleRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("业务规则不存在: " + id));
        return toResponse(rule);
    }

    @Override
    @Transactional
    public void updateRule(Long id, BusinessRuleRequest request) {
        BusinessRule rule = businessRuleRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("业务规则不存在: " + id));

        rule.setRuleName(request.getRuleName());
        rule.setRuleConfig(request.getRuleConfig());
        rule.setEnabled(request.getEnabled());
        rule.setDescription(request.getDescription());
        rule.setUpdatedAt(LocalDateTime.now());

        businessRuleRepository.save(rule);

        eventPublisher.publishEvent(ConfigChangedEvent.businessRuleChanged(
                this, rule.getId(), ConfigChangedEvent.OperationType.UPDATE));

        logger.info("更新业务规则: id={}, ruleName={}", id, request.getRuleName());
    }

    @Override
    @Transactional
    public void enableRule(Long id, boolean enabled) {
        BusinessRule rule = businessRuleRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("业务规则不存在: " + id));

        rule.setEnabled(enabled);
        rule.setUpdatedAt(LocalDateTime.now());
        businessRuleRepository.save(rule);

        eventPublisher.publishEvent(ConfigChangedEvent.businessRuleChanged(
                this, rule.getId(),
                enabled ? ConfigChangedEvent.OperationType.ENABLE : ConfigChangedEvent.OperationType.DISABLE));

        logger.info("{}业务规则: id={}", enabled ? "启用" : "禁用", id);
    }

    @Override
    public StateTransitionRule getActiveStateTransitionRule() {
        Long siteId = 0L;

        // 优先从缓存读取
        @SuppressWarnings("unchecked")
        StateTransitionRule cached = configCache.getBusinessRule(RULE_TYPE_STATE_TRANSITION);
        if (cached != null) {
            return cached;
        }

        BusinessRule rule = businessRuleRepository.findActiveRuleByType(siteId, RULE_TYPE_STATE_TRANSITION)
                .orElse(null);
        if (rule != null) {
            StateTransitionRule parsed = parseStateTransitionRule(rule);
            configCache.putBusinessRule(RULE_TYPE_STATE_TRANSITION, parsed);
            return parsed;
        }
        return createDefaultStateTransitionRule();
    }

    @Override
    public PdiStandardTimeConfig getPdiStandardTimeConfig() {
        Long siteId = 0L;

        // 优先从缓存读取
        @SuppressWarnings("unchecked")
        PdiStandardTimeConfig cached = configCache.getBusinessRule(RULE_TYPE_PDI_STANDARD_TIME);
        if (cached != null) {
            return cached;
        }

        BusinessRule rule = businessRuleRepository.findActiveRuleByType(siteId, RULE_TYPE_PDI_STANDARD_TIME)
                .orElse(null);
        if (rule != null) {
            PdiStandardTimeConfig parsed = parsePdiStandardTimeConfig(rule);
            configCache.putBusinessRule(RULE_TYPE_PDI_STANDARD_TIME, parsed);
            return parsed;
        }
        return createDefaultPdiStandardTimeConfig();
    }

    @Override
    public AlarmThresholdConfig getAlarmThresholdConfig() {
        Long siteId = 0L;

        // 优先从缓存读取
        @SuppressWarnings("unchecked")
        AlarmThresholdConfig cached = configCache.getBusinessRule(RULE_TYPE_ALARM_THRESHOLD);
        if (cached != null) {
            return cached;
        }

        BusinessRule rule = businessRuleRepository.findActiveRuleByType(siteId, RULE_TYPE_ALARM_THRESHOLD)
                .orElse(null);
        if (rule != null) {
            AlarmThresholdConfig parsed = parseAlarmThresholdConfig(rule);
            configCache.putBusinessRule(RULE_TYPE_ALARM_THRESHOLD, parsed);
            return parsed;
        }
        return createDefaultAlarmThresholdConfig();
    }

    /**
     * 解析状态转换规则
     */
    private StateTransitionRule parseStateTransitionRule(BusinessRule rule) {
        try {
            StateTransitionRule config = objectMapper.readValue(rule.getRuleConfig(), StateTransitionRule.class);
            config.setRuleCode(rule.getRuleCode());
            config.setRuleName(rule.getRuleName());
            config.setEnabled(rule.getEnabled());
            return config;
        } catch (JsonProcessingException e) {
            logger.error("解析状态转换规则失败: ruleId={}, error={}", rule.getId(), e.getMessage());
            return createDefaultStateTransitionRule();
        }
    }

    /**
     * 解析PDI标准工时配置
     */
    private PdiStandardTimeConfig parsePdiStandardTimeConfig(BusinessRule rule) {
        try {
            PdiStandardTimeConfig config = objectMapper.readValue(rule.getRuleConfig(), PdiStandardTimeConfig.class);
            config.setRuleCode(rule.getRuleCode());
            config.setRuleName(rule.getRuleName());
            config.setEnabled(rule.getEnabled());
            return config;
        } catch (JsonProcessingException e) {
            logger.error("解析PDI标准工时配置失败: ruleId={}, error={}", rule.getId(), e.getMessage());
            return createDefaultPdiStandardTimeConfig();
        }
    }

    /**
     * 解析告警阈值配置
     */
    private AlarmThresholdConfig parseAlarmThresholdConfig(BusinessRule rule) {
        try {
            AlarmThresholdConfig config = objectMapper.readValue(rule.getRuleConfig(), AlarmThresholdConfig.class);
            config.setRuleCode(rule.getRuleCode());
            config.setRuleName(rule.getRuleName());
            config.setEnabled(rule.getEnabled());
            return config;
        } catch (JsonProcessingException e) {
            logger.error("解析告警阈值配置失败: ruleId={}, error={}", rule.getId(), e.getMessage());
            return createDefaultAlarmThresholdConfig();
        }
    }

    /**
     * 转换为响应DTO
     */
    private BusinessRuleResponse toResponse(BusinessRule rule) {
        BusinessRuleResponse response = new BusinessRuleResponse();
        response.setId(rule.getId());
        response.setRuleName(rule.getRuleName());
        response.setRuleCode(rule.getRuleCode());
        response.setRuleType(rule.getRuleType() != null ? rule.getRuleType().name() : null);
        response.setRuleConfig(rule.getRuleConfig());
        response.setEnabled(rule.getEnabled());
        response.setDescription(rule.getDescription());
        response.setPriority(rule.getPriority());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        return response;
    }

    /**
     * 创建默认状态转换规则
     */
    private StateTransitionRule createDefaultStateTransitionRule() {
        StateTransitionRule rule = new StateTransitionRule();
        rule.setRuleCode("DEFAULT_STATE_TRANSITION");
        rule.setRuleName("默认状态转换规则");
        rule.setEnabled(true);
        // 默认转换规则
        rule.setTransitions(List.of(
                createTransition("INIT", "WORKING", "人员进入", "开始工作", true, 0),
                createTransition("WORKING", "PAUSE", "人员离开", "暂停计时", true, 0),
                createTransition("PAUSE", "WORKING", "人员返回", "恢复计时", true, 0),
                createTransition("WORKING", "COMPLETED", "工作完成", "正常完成", false, 0)
        ));
        return rule;
    }

    private StateTransitionRule.StateTransition createTransition(String from, String to, String condition,
                                                                  String desc, Boolean auto, Integer timeout) {
        StateTransitionRule.StateTransition transition = new StateTransitionRule.StateTransition();
        transition.setFromState(from);
        transition.setToState(to);
        transition.setCondition(condition);
        transition.setDescription(desc);
        transition.setAutoTransition(auto);
        transition.setTimeoutSeconds(timeout);
        return transition;
    }

    /**
     * 创建默认PDI标准工时配置
     */
    private PdiStandardTimeConfig createDefaultPdiStandardTimeConfig() {
        PdiStandardTimeConfig config = new PdiStandardTimeConfig();
        config.setRuleCode("DEFAULT_PDI_TIME");
        config.setRuleName("默认PDI标准工时配置");
        config.setEnabled(true);
        config.setDefaultStandardDuration(300);
        config.setOvertimeThresholdPercent(120);
        config.setChannelConfigs(List.of());
        return config;
    }

    /**
     * 创建默认告警阈值配置
     */
    private AlarmThresholdConfig createDefaultAlarmThresholdConfig() {
        AlarmThresholdConfig config = new AlarmThresholdConfig();
        config.setRuleCode("DEFAULT_ALARM_THRESHOLD");
        config.setRuleName("默认告警阈值配置");
        config.setEnabled(true);
        config.setSuppressSeconds(300);
        config.setMaxAlarmCount(0);

        // 默认告警级别阈值
        config.setLevelThresholds(List.of(
                createLevelThreshold("INFO", 0, 1, 0),
                createLevelThreshold("WARNING", 60, 3, 300),
                createLevelThreshold("ERROR", 180, 5, 600),
                createLevelThreshold("CRITICAL", 300, 10, 900)
        ));

        return config;
    }

    private AlarmThresholdConfig.AlarmLevelThreshold createLevelThreshold(String level, Integer duration,
                                                                           Integer count, Integer escalation) {
        AlarmThresholdConfig.AlarmLevelThreshold threshold = new AlarmThresholdConfig.AlarmLevelThreshold();
        threshold.setLevel(level);
        threshold.setDurationThreshold(duration);
        threshold.setCountThreshold(count);
        threshold.setEscalationSeconds(escalation);
        return threshold;
    }
}
