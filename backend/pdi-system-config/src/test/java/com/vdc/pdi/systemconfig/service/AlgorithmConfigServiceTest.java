package com.vdc.pdi.systemconfig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.systemconfig.cache.ConfigCache;
import com.vdc.pdi.systemconfig.domain.entity.AlgorithmConfig;
import com.vdc.pdi.systemconfig.domain.event.ConfigChangedEvent;
import com.vdc.pdi.systemconfig.domain.repository.AlgorithmConfigRepository;
import com.vdc.pdi.systemconfig.dto.request.AlgorithmConfigRequest;
import com.vdc.pdi.systemconfig.dto.response.AlgorithmConfigResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AlgorithmConfigService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AlgorithmConfigServiceTest {

    @Mock
    private AlgorithmConfigRepository algorithmConfigRepository;

    @Mock
    private ConfigCache configCache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ObjectMapper objectMapper;
    private AlgorithmConfigServiceImpl algorithmConfigService;

    private static final Long SITE_ID = 0L;
    private static final Long CHANNEL_ID = 1L;
    private static final String ALGORITHM_TYPE = "PDI_LEFT_FRONT";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        algorithmConfigService = new AlgorithmConfigServiceImpl(
                algorithmConfigRepository, objectMapper, configCache, eventPublisher);
    }

    private AlgorithmConfig createChannelConfig(Long id, boolean enabled) {
        AlgorithmConfig config = new AlgorithmConfig();
        config.setId(id);
        config.setSiteId(SITE_ID);
        config.setChannelId(CHANNEL_ID);
        config.setAlgorithmType(ALGORITHM_TYPE);
        config.setEnabled(enabled);
        config.setSensitivity("MEDIUM");
        config.setTriggerFrames(3);
        config.setStandardDuration(300);
        config.setEnterExitWindow(5);
        config.setPersonDisappearTimeout(10);
        config.setInheritGlobal(false);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return config;
    }

    private AlgorithmConfig createGlobalConfig(String algorithmType) {
        AlgorithmConfig config = new AlgorithmConfig();
        config.setId(100L);
        config.setSiteId(SITE_ID);
        config.setChannelId(null);
        config.setAlgorithmType(algorithmType);
        config.setEnabled(true);
        config.setSensitivity("HIGH");
        config.setTriggerFrames(5);
        config.setStandardDuration(600);
        config.setEnterExitWindow(10);
        config.setPersonDisappearTimeout(20);
        config.setInheritGlobal(false);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return config;
    }

    @Nested
    @DisplayName("获取配置测试")
    class GetConfigTest {

        @Test
        @DisplayName("应返回通道配置（当缓存命中时）")
        void shouldReturnCachedConfigWhenCacheHit() {
            // Given
            AlgorithmConfigResponse cachedResponse = new AlgorithmConfigResponse();
            cachedResponse.setChannelId(CHANNEL_ID);
            cachedResponse.setAlgorithmType(ALGORITHM_TYPE);
            cachedResponse.setEnabled(true);
            Map<String, AlgorithmConfigResponse> cachedMap = new HashMap<>();
            cachedMap.put(ALGORITHM_TYPE, cachedResponse);
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(cachedMap);

            // When
            AlgorithmConfigResponse response = algorithmConfigService.getConfig(CHANNEL_ID, ALGORITHM_TYPE);

            // Then
            assertNotNull(response);
            assertEquals(CHANNEL_ID, response.getChannelId());
            assertEquals(ALGORITHM_TYPE, response.getAlgorithmType());
            // 缓存命中时不应访问数据库
            verify(algorithmConfigRepository, never()).findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    anyLong(), anyLong(), anyString());
        }

        @Test
        @DisplayName("应返回通道配置（当缓存未命中但通道配置存在时）")
        void shouldReturnChannelConfigWhenExists() {
            // Given
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            AlgorithmConfig channelConfig = createChannelConfig(1L, true);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.of(channelConfig));

            // When
            AlgorithmConfigResponse response = algorithmConfigService.getConfig(CHANNEL_ID, ALGORITHM_TYPE);

            // Then
            assertNotNull(response);
            assertEquals(CHANNEL_ID, response.getChannelId());
            assertEquals(ALGORITHM_TYPE, response.getAlgorithmType());
            assertTrue(response.getEnabled());
            assertFalse(response.getInheritGlobal());
            // 验证缓存写入
            verify(configCache).putAlgorithmConfig(eq(CHANNEL_ID), anyMap());
        }

        @Test
        @DisplayName("应返回全局配置（当通道配置不存在但全局配置存在时）")
        void shouldReturnGlobalConfigWhenChannelConfigNotExists() {
            // Given
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            AlgorithmConfig globalConfig = createGlobalConfig(ALGORITHM_TYPE);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.empty());
            when(algorithmConfigRepository.findGlobalConfigByAlgorithmType(SITE_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.of(globalConfig));

            // When
            AlgorithmConfigResponse response = algorithmConfigService.getConfig(CHANNEL_ID, ALGORITHM_TYPE);

            // Then
            assertNotNull(response);
            assertNull(response.getChannelId());
            assertEquals(ALGORITHM_TYPE, response.getAlgorithmType());
            assertTrue(response.getInheritGlobal());
        }

        @Test
        @DisplayName("应返回默认配置（当通道和全局配置都不存在时）")
        void shouldReturnDefaultConfigWhenBothNotExist() {
            // Given
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.empty());
            when(algorithmConfigRepository.findGlobalConfigByAlgorithmType(SITE_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.empty());

            // When
            AlgorithmConfigResponse response = algorithmConfigService.getConfig(CHANNEL_ID, ALGORITHM_TYPE);

            // Then
            assertNotNull(response);
            assertEquals(ALGORITHM_TYPE, response.getAlgorithmType());
            assertTrue(response.getEnabled());
            assertTrue(response.getInheritGlobal());
            assertEquals("MEDIUM", response.getSensitivity());
            assertEquals(3, response.getTriggerFrames());
        }
    }

    @Nested
    @DisplayName("获取全局配置测试")
    class GetGlobalConfigTest {

        @Test
        @DisplayName("应返回全局配置")
        void shouldReturnGlobalConfig() {
            // Given
            AlgorithmConfig globalConfig = createGlobalConfig("SMOKE");
            when(algorithmConfigRepository.findGlobalConfigByAlgorithmType(SITE_ID, "SMOKE"))
                    .thenReturn(Optional.of(globalConfig));

            // When
            AlgorithmConfigResponse response = algorithmConfigService.getGlobalConfig("SMOKE");

            // Then
            assertNotNull(response);
            assertNull(response.getChannelId());
            assertEquals("SMOKE", response.getAlgorithmType());
            assertEquals("HIGH", response.getSensitivity());
        }

        @Test
        @DisplayName("全局配置不存在时应返回默认配置")
        void shouldReturnDefaultWhenGlobalConfigNotExists() {
            // Given
            when(algorithmConfigRepository.findGlobalConfigByAlgorithmType(SITE_ID, "SMOKE"))
                    .thenReturn(Optional.empty());

            // When
            AlgorithmConfigResponse response = algorithmConfigService.getGlobalConfig("SMOKE");

            // Then
            assertNotNull(response);
            assertNull(response.getChannelId());
            assertEquals("SMOKE", response.getAlgorithmType());
            assertTrue(response.getEnabled());
        }
    }

    @Nested
    @DisplayName("更新配置测试")
    class UpdateConfigTest {

        @Test
        @DisplayName("应成功更新通道配置并发布事件")
        void shouldUpdateChannelConfigAndPublishEvent() {
            // Given
            AlgorithmConfig existingConfig = createChannelConfig(1L, true);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.of(existingConfig));
            when(algorithmConfigRepository.save(any(AlgorithmConfig.class))).thenReturn(existingConfig);

            AlgorithmConfigRequest request = new AlgorithmConfigRequest();
            request.setAlgorithmType(ALGORITHM_TYPE);
            request.setEnabled(false);
            request.setSensitivity("LOW");
            request.setTriggerFrames(5);
            request.setStandardDuration(600);

            // When
            algorithmConfigService.updateConfig(CHANNEL_ID, request);

            // Then
            verify(algorithmConfigRepository).save(argThat(saved ->
                    !saved.getEnabled() &&
                            saved.getSensitivity().equals("LOW") &&
                            saved.getTriggerFrames() == 5 &&
                            saved.getStandardDuration() == 600
            ));
            // 验证事件发布
            ArgumentCaptor<ConfigChangedEvent> eventCaptor = ArgumentCaptor.forClass(ConfigChangedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            ConfigChangedEvent event = eventCaptor.getValue();
            assertEquals(ConfigChangedEvent.ConfigType.ALGORITHM, event.getConfigType());
            assertEquals(ConfigChangedEvent.OperationType.UPDATE, event.getOperationType());
            assertEquals(CHANNEL_ID, event.getChannelId());
        }

        @Test
        @DisplayName("配置不存在时应创建新配置并发布事件")
        void shouldCreateNewConfigWhenNotExists() {
            // Given
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.empty());
            AlgorithmConfig savedConfig = new AlgorithmConfig();
            savedConfig.setId(1L);
            when(algorithmConfigRepository.save(any(AlgorithmConfig.class)))
                    .thenReturn(savedConfig);

            AlgorithmConfigRequest request = new AlgorithmConfigRequest();
            request.setAlgorithmType(ALGORITHM_TYPE);
            request.setEnabled(true);
            request.setSensitivity("HIGH");

            // When
            algorithmConfigService.updateConfig(CHANNEL_ID, request);

            // Then
            verify(algorithmConfigRepository).save(argThat(saved ->
                    saved.getChannelId().equals(CHANNEL_ID) &&
                            saved.getAlgorithmType().equals(ALGORITHM_TYPE) &&
                            saved.getSensitivity().equals("HIGH")
            ));
            // 验证事件发布
            verify(eventPublisher).publishEvent(any(ConfigChangedEvent.class));
        }
    }

    @Nested
    @DisplayName("分页查询测试")
    class ListChannelConfigsTest {

        @Test
        @DisplayName("应返回分页配置列表")
        void shouldReturnPagedConfigs() {
            // Given
            AlgorithmConfig config1 = createChannelConfig(1L, true);
            AlgorithmConfig config2 = createChannelConfig(2L, true);
            config2.setAlgorithmType("PDI_LEFT_REAR");
            Page<AlgorithmConfig> page = new PageImpl<>(List.of(config1, config2), PageRequest.of(0, 20), 2);

            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndDeletedAtIsNull(
                    eq(SITE_ID), eq(CHANNEL_ID), any(Pageable.class)))
                    .thenReturn(page);

            // When
            PageResult<AlgorithmConfigResponse> result = algorithmConfigService.listChannelConfigs(CHANNEL_ID, 1, 20);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getTotal());
            assertEquals(2, result.getList().size());
        }
    }

    @Nested
    @DisplayName("规则引擎调用方法测试")
    class RuleEngineMethodsTest {

        @Test
        @DisplayName("应返回缓存的算法启用状态")
        void shouldReturnCachedAlgorithmEnabledStatus() {
            // Given
            when(configCache.getAlgorithmSwitch(CHANNEL_ID, ALGORITHM_TYPE)).thenReturn(true);

            // When
            boolean enabled = algorithmConfigService.isAlgorithmEnabled(CHANNEL_ID, ALGORITHM_TYPE);

            // Then
            assertTrue(enabled);
            // 缓存命中时不应访问数据库
            verify(algorithmConfigRepository, never()).findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    anyLong(), anyLong(), anyString());
        }

        @Test
        @DisplayName("缓存未命中时应从数据库读取并写入缓存")
        void shouldReadFromDbAndWriteToCacheWhenCacheMiss() {
            // Given
            when(configCache.getAlgorithmSwitch(CHANNEL_ID, ALGORITHM_TYPE)).thenReturn(null);
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            AlgorithmConfig config = createChannelConfig(1L, true);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.of(config));

            // When
            boolean enabled = algorithmConfigService.isAlgorithmEnabled(CHANNEL_ID, ALGORITHM_TYPE);

            // Then
            assertTrue(enabled);
            verify(configCache).putAlgorithmSwitch(CHANNEL_ID, ALGORITHM_TYPE, true);
        }

        @Test
        @DisplayName("配置不存在时应返回true（默认启用）并写入缓存")
        void shouldReturnTrueWhenConfigNotExists() {
            // Given
            when(configCache.getAlgorithmSwitch(CHANNEL_ID, ALGORITHM_TYPE)).thenReturn(null);
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.empty());
            when(algorithmConfigRepository.findGlobalConfigByAlgorithmType(SITE_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.empty());

            // When
            boolean enabled = algorithmConfigService.isAlgorithmEnabled(CHANNEL_ID, ALGORITHM_TYPE);

            // Then
            assertTrue(enabled);
            verify(configCache).putAlgorithmSwitch(CHANNEL_ID, ALGORITHM_TYPE, true);
        }

        @Test
        @DisplayName("应返回标准工时")
        void shouldReturnStandardDuration() {
            // Given
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            AlgorithmConfig config = createChannelConfig(1L, true);
            config.setStandardDuration(300);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.of(config));

            // When
            Integer duration = algorithmConfigService.getStandardDuration(CHANNEL_ID);

            // Then
            assertNotNull(duration);
            assertEquals(300, duration);
        }

        @Test
        @DisplayName("应返回人员消失超时阈值")
        void shouldReturnPersonDisappearTimeout() {
            // Given
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            AlgorithmConfig config = createChannelConfig(1L, true);
            config.setPersonDisappearTimeout(15);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.of(config));

            // When
            Integer timeout = algorithmConfigService.getPersonDisappearTimeout(CHANNEL_ID);

            // Then
            assertNotNull(timeout);
            assertEquals(15, timeout);
        }

        @Test
        @DisplayName("应返回进出判定时间窗口")
        void shouldReturnEnterExitWindow() {
            // Given
            when(configCache.getAlgorithmConfig(CHANNEL_ID)).thenReturn(null);
            AlgorithmConfig config = createChannelConfig(1L, true);
            config.setEnterExitWindow(8);
            when(algorithmConfigRepository.findBySiteIdAndChannelIdAndAlgorithmTypeAndDeletedAtIsNull(
                    SITE_ID, CHANNEL_ID, ALGORITHM_TYPE))
                    .thenReturn(Optional.of(config));

            // When
            Integer window = algorithmConfigService.getEnterExitWindow(CHANNEL_ID);

            // Then
            assertNotNull(window);
            assertEquals(8, window);
        }
    }
}
