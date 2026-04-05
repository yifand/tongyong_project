package com.vdc.pdi.systemconfig.service;

import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.systemconfig.dto.request.BusinessRuleRequest;
import com.vdc.pdi.systemconfig.dto.response.BusinessRuleResponse;
import com.vdc.pdi.systemconfig.dto.rule.AlarmThresholdConfig;
import com.vdc.pdi.systemconfig.dto.rule.PdiStandardTimeConfig;
import com.vdc.pdi.systemconfig.dto.rule.StateTransitionRule;

/**
 * 业务规则服务接口
 */
public interface BusinessRuleService {

    /**
     * 分页查询业务规则
     *
     * @param page     页码
     * @param size     每页大小
     * @param ruleType 规则类型（可选）
     * @return 分页结果
     */
    PageResult<BusinessRuleResponse> listRules(int page, int size, String ruleType);

    /**
     * 获取业务规则详情
     *
     * @param id 规则ID
     * @return 规则响应
     */
    BusinessRuleResponse getRule(Long id);

    /**
     * 更新业务规则
     *
     * @param id      规则ID
     * @param request 规则请求
     */
    void updateRule(Long id, BusinessRuleRequest request);

    /**
     * 启用/禁用规则
     *
     * @param id      规则ID
     * @param enabled 是否启用
     */
    void enableRule(Long id, boolean enabled);

    /**
     * 获取有效的状态转换规则（供规则引擎调用）
     *
     * @return 状态转换规则配置
     */
    StateTransitionRule getActiveStateTransitionRule();

    /**
     * 获取PDI标准工时配置（供规则引擎调用）
     *
     * @return PDI标准工时配置
     */
    PdiStandardTimeConfig getPdiStandardTimeConfig();

    /**
     * 获取告警阈值配置（供规则引擎调用）
     *
     * @return 告警阈值配置
     */
    AlarmThresholdConfig getAlarmThresholdConfig();
}
