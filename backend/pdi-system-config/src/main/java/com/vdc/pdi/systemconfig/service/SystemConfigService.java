package com.vdc.pdi.systemconfig.service;

import com.vdc.pdi.systemconfig.dto.request.GeneralConfigRequest;
import com.vdc.pdi.systemconfig.dto.response.ConfigGroupResponse;
import com.vdc.pdi.systemconfig.dto.response.ConfigResponse;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {

    /**
     * 获取通用配置分组
     *
     * @param configGroup 配置分组
     * @return 分组配置响应
     */
    ConfigGroupResponse getGeneralConfig(String configGroup);

    /**
     * 批量更新通用配置
     *
     * @param request 配置请求
     */
    void updateGeneralConfig(GeneralConfigRequest request);

    /**
     * 根据配置键获取配置
     *
     * @param configKey 配置键
     * @return 配置响应
     */
    ConfigResponse getConfigByKey(String configKey);

    /**
     * 获取字符串类型的配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getStringValue(String configKey, String defaultValue);

    /**
     * 获取整数类型的配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer getIntValue(String configKey, Integer defaultValue);

    /**
     * 获取布尔类型的配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Boolean getBooleanValue(String configKey, Boolean defaultValue);

    /**
     * 获取JSON类型的配置值
     *
     * @param configKey 配置键
     * @param clazz     目标类型
     * @param <T>       泛型类型
     * @return 配置值
     */
    <T> T getJsonValue(String configKey, Class<T> clazz);

    /**
     * 获取JSON类型的配置值（带默认值）
     *
     * @param configKey    配置键
     * @param clazz        目标类型
     * @param defaultValue 默认值
     * @param <T>          泛型类型
     * @return 配置值
     */
    <T> T getJsonValue(String configKey, Class<T> clazz, T defaultValue);

    /**
     * 获取所有配置分组
     *
     * @return 配置分组列表
     */
    List<String> listConfigGroups();
}
