package com.vdc.pdi.systemconfig.controller;

import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.systemconfig.dto.request.AlgorithmConfigRequest;
import com.vdc.pdi.systemconfig.dto.request.BusinessRuleRequest;
import com.vdc.pdi.systemconfig.dto.request.GeneralConfigRequest;
import com.vdc.pdi.systemconfig.dto.response.AlgorithmConfigResponse;
import com.vdc.pdi.systemconfig.dto.response.BusinessRuleResponse;
import com.vdc.pdi.systemconfig.dto.response.ConfigGroupResponse;
import com.vdc.pdi.systemconfig.dto.response.ConfigResponse;
import com.vdc.pdi.systemconfig.service.AlgorithmConfigService;
import com.vdc.pdi.systemconfig.service.BusinessRuleService;
import com.vdc.pdi.systemconfig.service.SystemConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.systemconfig.TestConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ConfigController 单元测试
 */
@WebMvcTest(ConfigController.class)
@ContextConfiguration(classes = {TestConfig.class, ConfigController.class})
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlgorithmConfigService algorithmConfigService;

    @MockBean
    private BusinessRuleService businessRuleService;

    @MockBean
    private SystemConfigService systemConfigService;

    @Nested
    @DisplayName("算法配置接口测试")
    class AlgorithmConfigTest {

        @Test
        @DisplayName("应返回200并获取通道算法配置")
        @WithMockUser
        void shouldGetChannelAlgorithmConfig() throws Exception {
            // Given
            AlgorithmConfigResponse response = new AlgorithmConfigResponse();
            response.setChannelId(1L);
            response.setAlgorithmType("PDI_LEFT_FRONT");
            response.setEnabled(true);
            response.setSensitivity("MEDIUM");
            when(algorithmConfigService.getConfig(1L, "PDI_LEFT_FRONT")).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/config/algorithm/{channelId}", 1L)
                            .param("algorithmType", "PDI_LEFT_FRONT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.channelId").value(1))
                    .andExpect(jsonPath("$.data.algorithmType").value("PDI_LEFT_FRONT"))
                    .andExpect(jsonPath("$.data.enabled").value(true));
        }

        @Test
        @DisplayName("ADMIN角色应成功更新通道算法配置")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateChannelAlgorithmConfigWithAdminRole() throws Exception {
            // Given
            AlgorithmConfigRequest request = new AlgorithmConfigRequest();
            request.setAlgorithmType("PDI_LEFT_FRONT");
            request.setEnabled(false);
            request.setSensitivity("HIGH");

            // When & Then
            mockMvc.perform(put("/api/v1/config/algorithm/{channelId}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(algorithmConfigService).updateConfig(eq(1L), any(AlgorithmConfigRequest.class));
        }

        @Test
        @DisplayName("非ADMIN角色应403拒绝更新通道算法配置")
        @WithMockUser(roles = "USER")
        void shouldRejectUpdateWithoutAdminRole() throws Exception {
            // Given
            AlgorithmConfigRequest request = new AlgorithmConfigRequest();
            request.setAlgorithmType("PDI_LEFT_FRONT");
            request.setEnabled(false);

            // When & Then
            mockMvc.perform(put("/api/v1/config/algorithm/{channelId}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("应返回200并获取全局算法配置")
        @WithMockUser
        void shouldGetGlobalAlgorithmConfig() throws Exception {
            // Given
            AlgorithmConfigResponse response = new AlgorithmConfigResponse();
            response.setAlgorithmType("SMOKE");
            response.setEnabled(true);
            when(algorithmConfigService.getGlobalConfig("SMOKE")).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/config/algorithm/global")
                            .param("algorithmType", "SMOKE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.algorithmType").value("SMOKE"));
        }

        @Test
        @DisplayName("SUPER_ADMIN角色应成功更新全局算法配置")
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldUpdateGlobalAlgorithmConfigWithSuperAdminRole() throws Exception {
            // Given
            AlgorithmConfigRequest request = new AlgorithmConfigRequest();
            request.setAlgorithmType("SMOKE");
            request.setEnabled(true);
            request.setSensitivity("LOW");

            // When & Then
            mockMvc.perform(put("/api/v1/config/algorithm/global")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(algorithmConfigService).updateGlobalConfig(any(AlgorithmConfigRequest.class));
        }
    }

    @Nested
    @DisplayName("业务规则接口测试")
    class BusinessRuleTest {

        @Test
        @DisplayName("应返回200并获取业务规则列表")
        @WithMockUser
        void shouldGetRulesList() throws Exception {
            // Given
            BusinessRuleResponse rule = new BusinessRuleResponse();
            rule.setId(1L);
            rule.setRuleName("测试规则");
            rule.setRuleType("STATE_TRANSITION");
            rule.setEnabled(true);
            PageResult<BusinessRuleResponse> pageResult = PageResult.of(
                    List.of(rule), 1, 1, 20);
            when(businessRuleService.listRules(1, 20, null)).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/api/v1/config/rules")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list[0].id").value(1))
                    .andExpect(jsonPath("$.data.total").value(1));
        }

        @Test
        @DisplayName("应返回200并获取单个业务规则")
        @WithMockUser
        void shouldGetSingleRule() throws Exception {
            // Given
            BusinessRuleResponse rule = new BusinessRuleResponse();
            rule.setId(1L);
            rule.setRuleName("测试规则");
            when(businessRuleService.getRule(1L)).thenReturn(rule);

            // When & Then
            mockMvc.perform(get("/api/v1/config/rules/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.ruleName").value("测试规则"));
        }

        @Test
        @DisplayName("ADMIN角色应成功更新业务规则")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateRuleWithAdminRole() throws Exception {
            // Given
            BusinessRuleRequest request = new BusinessRuleRequest();
            request.setRuleName("更新后的规则");
            request.setRuleConfig("{\"key\": \"value\"}");
            request.setEnabled(true);

            // When & Then
            mockMvc.perform(put("/api/v1/config/rules/{id}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(businessRuleService).updateRule(eq(1L), any(BusinessRuleRequest.class));
        }

        @Test
        @DisplayName("ADMIN角色应成功启用业务规则")
        @WithMockUser(roles = "ADMIN")
        void shouldEnableRuleWithAdminRole() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/config/rules/{id}/enable", 1L)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(businessRuleService).enableRule(1L, true);
        }

        @Test
        @DisplayName("ADMIN角色应成功禁用业务规则")
        @WithMockUser(roles = "ADMIN")
        void shouldDisableRuleWithAdminRole() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/config/rules/{id}/disable", 1L)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(businessRuleService).enableRule(1L, false);
        }
    }

    @Nested
    @DisplayName("通用配置接口测试")
    class GeneralConfigTest {

        @Test
        @DisplayName("应返回200并获取通用配置")
        @WithMockUser
        void shouldGetGeneralConfig() throws Exception {
            // Given
            ConfigResponse config = new ConfigResponse();
            config.setId(1L);
            config.setConfigKey("system.name");
            config.setConfigValue("PDI系统");
            config.setConfigGroup("system");
            ConfigGroupResponse response = new ConfigGroupResponse();
            response.setConfigGroup("system");
            response.setConfigs(List.of(config));
            when(systemConfigService.getGeneralConfig("system")).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/config/general")
                            .param("configGroup", "system"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.configGroup").value("system"))
                    .andExpect(jsonPath("$.data.configs[0].configKey").value("system.name"));
        }

        @Test
        @DisplayName("应返回200并获取配置分组列表")
        @WithMockUser
        void shouldGetConfigGroups() throws Exception {
            // Given
            when(systemConfigService.listConfigGroups()).thenReturn(List.of("system", "alarm", "retention"));

            // When & Then
            mockMvc.perform(get("/api/v1/config/general/groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0]").value("system"))
                    .andExpect(jsonPath("$.data[1]").value("alarm"));
        }

        @Test
        @DisplayName("应返回200并获取指定配置项")
        @WithMockUser
        void shouldGetConfigByKey() throws Exception {
            // Given
            ConfigResponse config = new ConfigResponse();
            config.setId(1L);
            config.setConfigKey("system.name");
            config.setConfigValue("PDI系统");
            when(systemConfigService.getConfigByKey("system.name")).thenReturn(config);

            // When & Then
            mockMvc.perform(get("/api/v1/config/general/{configKey}", "system.name"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.configKey").value("system.name"))
                    .andExpect(jsonPath("$.data.configValue").value("PDI系统"));
        }

        @Test
        @DisplayName("ADMIN角色应成功更新通用配置")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateGeneralConfigWithAdminRole() throws Exception {
            // Given
            GeneralConfigRequest request = new GeneralConfigRequest();
            request.setConfigGroup("system");
            GeneralConfigRequest.ConfigItem item = new GeneralConfigRequest.ConfigItem();
            item.setConfigKey("system.name");
            item.setConfigValue("新系统名");
            request.setConfigs(List.of(item));

            // When & Then
            mockMvc.perform(put("/api/v1/config/general")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(systemConfigService).updateGeneralConfig(any(GeneralConfigRequest.class));
        }
    }
}
