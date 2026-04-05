package com.vdc.pdi.systemconfig.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BusinessRuleService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class BusinessRuleServiceTest {

    @Mock
    private BusinessRuleRepository businessRuleRepository;

    @Mock
    private ConfigCache configCache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ObjectMapper objectMapper;
    private BusinessRuleServiceImpl businessRuleService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        businessRuleService = new BusinessRuleServiceImpl(
                businessRuleRepository, objectMapper, configCache, eventPublisher);
    }

    private BusinessRule createMockRule(Long id, String ruleCode, BusinessRule.RuleType ruleType, String ruleConfig) {
        BusinessRule rule = new BusinessRule();
        rule.setId(id);
        rule.setSiteId(0L);
        rule.setRuleName("测试规则" + id);
        rule.setRuleCode(ruleCode);
        rule.setRuleType(ruleType);
        rule.setRuleConfig(ruleConfig);
        rule.setEnabled(true);
        rule.setDescription("测试描述");
        rule.setPriority(0);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        return rule;
    }

    @Nested
    @DisplayName("分页查询测试")
    class ListRulesTest {

        @Test
        @DisplayName("应成功返回分页业务规则列表（无规则类型过滤）")
        void shouldReturnPagedRulesWithoutFilter() {
            // Given
            BusinessRule rule1 = createMockRule(1L, "RULE_001", BusinessRule.RuleType.STATE_TRANSITION, "{}");
            BusinessRule rule2 = createMockRule(2L, "RULE_002", BusinessRule.RuleType.ALARM_THRESHOLD, "{}");
            Page<BusinessRule> page = new PageImpl<>(List.of(rule1, rule2), PageRequest.of(0, 20), 2);

            when(businessRuleRepository.findBySiteIdAndDeletedAtIsNull(eq(0L), any(Pageable.class)))
                    .thenReturn(page);

            // When
            PageResult<BusinessRuleResponse> result = businessRuleService.listRules(1, 20, null);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getTotal());
            assertEquals(1, result.getPage());
            assertEquals(20, result.getSize());
            assertEquals(2, result.getList().size());
            verify(businessRuleRepository).findBySiteIdAndDeletedAtIsNull(eq(0L), any(Pageable.class));
        }

        @Test
        @DisplayName("应成功返回分页业务规则列表（有规则类型过滤）")
        void shouldReturnPagedRulesWithFilter() {
            // Given
            BusinessRule rule = createMockRule(1L, "RULE_001", BusinessRule.RuleType.STATE_TRANSITION, "{}");
            Page<BusinessRule> page = new PageImpl<>(List.of(rule), PageRequest.of(0, 20), 1);

            when(businessRuleRepository.findBySiteIdAndRuleTypeAndDeletedAtIsNull(
                    eq(0L), eq("STATE_TRANSITION"), any(Pageable.class)))
                    .thenReturn(page);

            // When
            PageResult<BusinessRuleResponse> result = businessRuleService.listRules(1, 20, "STATE_TRANSITION");

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
            assertEquals("STATE_TRANSITION", result.getList().get(0).getRuleType());
        }
    }

    @Nested
    @DisplayName("获取规则详情测试")
    class GetRuleTest {

        @Test
        @DisplayName("应成功返回规则详情")
        void shouldReturnRuleDetail() {
            // Given
            BusinessRule rule = createMockRule(1L, "RULE_001", BusinessRule.RuleType.STATE_TRANSITION, "{}");
            when(businessRuleRepository.findById(1L)).thenReturn(Optional.of(rule));

            // When
            BusinessRuleResponse response = businessRuleService.getRule(1L);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("RULE_001", response.getRuleCode());
            assertEquals("STATE_TRANSITION", response.getRuleType());
        }

        @Test
        @DisplayName("规则不存在时应抛出异常")
        void shouldThrowExceptionWhenRuleNotFound() {
            // Given
            when(businessRuleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> businessRuleService.getRule(999L));
            assertTrue(exception.getMessage().contains("业务规则不存在"));
        }

        @Test
        @DisplayName("已删除规则应抛出异常")
        void shouldThrowExceptionWhenRuleIsDeleted() {
            // Given
            BusinessRule rule = createMockRule(1L, "RULE_001", BusinessRule.RuleType.STATE_TRANSITION, "{}");
            rule.setDeletedAt(LocalDateTime.now());
            when(businessRuleRepository.findById(1L)).thenReturn(Optional.of(rule));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> businessRuleService.getRule(1L));
            assertTrue(exception.getMessage().contains("业务规则不存在"));
        }
    }

    @Nested
    @DisplayName("更新规则测试")
    class UpdateRuleTest {

        @Test
        @DisplayName("应成功更新规则并发布事件")
        void shouldUpdateRuleAndPublishEvent() {
            // Given
            BusinessRule rule = createMockRule(1L, "RULE_001", BusinessRule.RuleType.STATE_TRANSITION, "{}");
            when(businessRuleRepository.findById(1L)).thenReturn(Optional.of(rule));
            when(businessRuleRepository.save(any(BusinessRule.class))).thenReturn(rule);

            BusinessRuleRequest request = new BusinessRuleRequest();
            request.setRuleName("更新后的规则名");
            request.setRuleConfig("{\"updated\": true}");
            request.setEnabled(false);
            request.setDescription("更新后的描述");

            // When
            businessRuleService.updateRule(1L, request);

            // Then
            verify(businessRuleRepository).save(argThat(saved ->
                    saved.getRuleName().equals("更新后的规则名") &&
                            saved.getRuleConfig().equals("{\"updated\": true}") &&
                            !saved.getEnabled() &&
                            saved.getDescription().equals("更新后的描述")
            ));
            // 验证事件发布
            ArgumentCaptor<ConfigChangedEvent> eventCaptor = ArgumentCaptor.forClass(ConfigChangedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            ConfigChangedEvent event = eventCaptor.getValue();
            assertEquals(ConfigChangedEvent.ConfigType.BUSINESS_RULE, event.getConfigType());
            assertEquals(ConfigChangedEvent.OperationType.UPDATE, event.getOperationType());
            assertEquals(1L, event.getConfigId());
        }

        @Test
        @DisplayName("更新不存在的规则应抛出异常")
        void shouldThrowExceptionWhenUpdatingNonExistentRule() {
            // Given
            when(businessRuleRepository.findById(999L)).thenReturn(Optional.empty());

            BusinessRuleRequest request = new BusinessRuleRequest();
            request.setRuleName("新规则名");

            // When & Then
            assertThrows(BusinessException.class,
                    () -> businessRuleService.updateRule(999L, request));
        }
    }

    @Nested
    @DisplayName("启用/禁用规则测试")
    class EnableRuleTest {

        @Test
        @DisplayName("应成功启用规则并发布ENABLE事件")
        void shouldEnableRuleAndPublishEvent() {
            // Given
            BusinessRule rule = createMockRule(1L, "RULE_001", BusinessRule.RuleType.STATE_TRANSITION, "{}");
            rule.setEnabled(false);
            when(businessRuleRepository.findById(1L)).thenReturn(Optional.of(rule));
            when(businessRuleRepository.save(any(BusinessRule.class))).thenReturn(rule);

            // When
            businessRuleService.enableRule(1L, true);

            // Then
            verify(businessRuleRepository).save(argThat(saved -> saved.getEnabled()));
            // 验证事件发布
            ArgumentCaptor<ConfigChangedEvent> eventCaptor = ArgumentCaptor.forClass(ConfigChangedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            ConfigChangedEvent event = eventCaptor.getValue();
            assertEquals(ConfigChangedEvent.ConfigType.BUSINESS_RULE, event.getConfigType());
            assertEquals(ConfigChangedEvent.OperationType.ENABLE, event.getOperationType());
        }

        @Test
        @DisplayName("应成功禁用规则并发布DISABLE事件")
        void shouldDisableRuleAndPublishEvent() {
            // Given
            BusinessRule rule = createMockRule(1L, "RULE_001", BusinessRule.RuleType.STATE_TRANSITION, "{}");
            rule.setEnabled(true);
            when(businessRuleRepository.findById(1L)).thenReturn(Optional.of(rule));
            when(businessRuleRepository.save(any(BusinessRule.class))).thenReturn(rule);

            // When
            businessRuleService.enableRule(1L, false);

            // Then
            verify(businessRuleRepository).save(argThat(saved -> !saved.getEnabled()));
            // 验证事件发布
            ArgumentCaptor<ConfigChangedEvent> eventCaptor = ArgumentCaptor.forClass(ConfigChangedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            ConfigChangedEvent event = eventCaptor.getValue();
            assertEquals(ConfigChangedEvent.ConfigType.BUSINESS_RULE, event.getConfigType());
            assertEquals(ConfigChangedEvent.OperationType.DISABLE, event.getOperationType());
        }
    }

    @Nested
    @DisplayName("规则引擎配置获取测试")
    class RuleEngineConfigTest {

        @Test
        @DisplayName("应返回缓存中的状态转换规则")
        void shouldReturnCachedStateTransitionRule() {
            // Given
            StateTransitionRule cachedRule = new StateTransitionRule();
            cachedRule.setRuleCode("DEFAULT_STATE_TRANSITION");
            cachedRule.setEnabled(true);
            when(configCache.getBusinessRule("STATE_TRANSITION")).thenReturn(cachedRule);

            // When
            StateTransitionRule result = businessRuleService.getActiveStateTransitionRule();

            // Then
            assertNotNull(result);
            assertEquals("DEFAULT_STATE_TRANSITION", result.getRuleCode());
            // 缓存命中时不应访问数据库
            verify(businessRuleRepository, never()).findActiveRuleByType(anyLong(), anyString());
        }

        @Test
        @DisplayName("缓存未命中时应从数据库读取并写入缓存")
        void shouldReadFromDbAndWriteToCacheWhenCacheMiss() throws Exception {
            // Given
            BusinessRule rule = createMockRule(1L, "DEFAULT_STATE_TRANSITION", BusinessRule.RuleType.STATE_TRANSITION,
                    "{\"transitions\": [{\"fromState\": \"INIT\", \"toState\": \"WORKING\"}]}");
            when(configCache.getBusinessRule("STATE_TRANSITION")).thenReturn(null);
            when(businessRuleRepository.findActiveRuleByType(0L, "STATE_TRANSITION"))
                    .thenReturn(Optional.of(rule));

            // When
            StateTransitionRule result = businessRuleService.getActiveStateTransitionRule();

            // Then
            assertNotNull(result);
            assertEquals("DEFAULT_STATE_TRANSITION", result.getRuleCode());
            // 验证缓存写入
            verify(configCache).putBusinessRule(eq("STATE_TRANSITION"), any(StateTransitionRule.class));
        }

        @Test
        @DisplayName("无有效状态转换规则时应返回默认规则")
        void shouldReturnDefaultStateTransitionRuleWhenNoneActive() {
            // Given
            when(configCache.getBusinessRule("STATE_TRANSITION")).thenReturn(null);
            when(businessRuleRepository.findActiveRuleByType(0L, "STATE_TRANSITION"))
                    .thenReturn(Optional.empty());

            // When
            StateTransitionRule result = businessRuleService.getActiveStateTransitionRule();

            // Then
            assertNotNull(result);
            assertEquals("DEFAULT_STATE_TRANSITION", result.getRuleCode());
            assertTrue(result.getEnabled());
            assertNotNull(result.getTransitions());
        }

        @Test
        @DisplayName("应返回缓存中的PDI标准工时配置")
        void shouldReturnCachedPdiStandardTimeConfig() {
            // Given
            PdiStandardTimeConfig cachedConfig = new PdiStandardTimeConfig();
            cachedConfig.setDefaultStandardDuration(300);
            when(configCache.getBusinessRule("PDI_STANDARD_TIME")).thenReturn(cachedConfig);

            // When
            PdiStandardTimeConfig result = businessRuleService.getPdiStandardTimeConfig();

            // Then
            assertNotNull(result);
            assertEquals(300, result.getDefaultStandardDuration());
            // 缓存命中时不应访问数据库
            verify(businessRuleRepository, never()).findActiveRuleByType(anyLong(), anyString());
        }

        @Test
        @DisplayName("应返回告警阈值配置并写入缓存")
        void shouldReturnAlarmThresholdConfigAndWriteToCache() throws Exception {
            // Given
            BusinessRule rule = createMockRule(1L, "DEFAULT_ALARM_THRESHOLD", BusinessRule.RuleType.ALARM_THRESHOLD,
                    "{\"suppressSeconds\": 300}");
            when(configCache.getBusinessRule("ALARM_THRESHOLD")).thenReturn(null);
            when(businessRuleRepository.findActiveRuleByType(0L, "ALARM_THRESHOLD"))
                    .thenReturn(Optional.of(rule));

            // When
            AlarmThresholdConfig result = businessRuleService.getAlarmThresholdConfig();

            // Then
            assertNotNull(result);
            assertEquals(300, result.getSuppressSeconds());
            // 验证缓存写入
            verify(configCache).putBusinessRule(eq("ALARM_THRESHOLD"), any(AlarmThresholdConfig.class));
        }

        @Test
        @DisplayName("解析失败时应返回默认配置")
        void shouldReturnDefaultConfigWhenParsingFails() throws Exception {
            // Given
            BusinessRule rule = createMockRule(1L, "INVALID_RULE", BusinessRule.RuleType.STATE_TRANSITION, "invalid json");
            when(configCache.getBusinessRule("STATE_TRANSITION")).thenReturn(null);
            when(businessRuleRepository.findActiveRuleByType(0L, "STATE_TRANSITION"))
                    .thenReturn(Optional.of(rule));

            // When
            StateTransitionRule result = businessRuleService.getActiveStateTransitionRule();

            // Then
            assertNotNull(result);
            assertEquals("DEFAULT_STATE_TRANSITION", result.getRuleCode());
        }
    }
}
