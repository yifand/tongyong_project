package com.vdc.pdi.systemconfig.service;

import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.systemconfig.dto.request.AlgorithmConfigRequest;
import com.vdc.pdi.systemconfig.dto.response.AlgorithmConfigResponse;

/**
 * 算法配置服务接口
 */
public interface AlgorithmConfigService {

    /**
     * 获取通道算法配置
     * 优先返回通道配置，不存在则返回全局配置
     *
     * @param channelId     通道ID（null表示获取全局配置）
     * @param algorithmType 算法类型（SMOKE/PDI_LEFT_FRONT/PDI_LEFT_REAR/PDI_SLIDE）
     * @return 算法配置响应
     */
    AlgorithmConfigResponse getConfig(Long channelId, String algorithmType);

    /**
     * 更新通道算法配置
     *
     * @param channelId 通道ID
     * @param request   配置请求
     */
    void updateConfig(Long channelId, AlgorithmConfigRequest request);

    /**
     * 获取全局算法配置
     *
     * @param algorithmType 算法类型
     * @return 全局配置响应
     */
    AlgorithmConfigResponse getGlobalConfig(String algorithmType);

    /**
     * 更新全局算法配置
     *
     * @param request 配置请求
     */
    void updateGlobalConfig(AlgorithmConfigRequest request);

    /**
     * 分页查询通道算法配置
     *
     * @param channelId 通道ID
     * @param page      页码
     * @param size      每页大小
     * @return 分页结果
     */
    PageResult<AlgorithmConfigResponse> listChannelConfigs(Long channelId, int page, int size);

    /**
     * 检查算法是否启用（供规则引擎调用）
     *
     * @param channelId     通道ID
     * @param algorithmType 算法类型
     * @return 是否启用
     */
    boolean isAlgorithmEnabled(Long channelId, String algorithmType);

    /**
     * 获取标准工时（供规则引擎调用，PDI算法专用）
     *
     * @param channelId 通道ID
     * @return 标准工时（秒）
     */
    Integer getStandardDuration(Long channelId);

    /**
     * 获取人员消失超时阈值
     *
     * @param channelId 通道ID
     * @return 超时阈值（秒）
     */
    Integer getPersonDisappearTimeout(Long channelId);

    /**
     * 获取进出判定时间窗口
     *
     * @param channelId 通道ID
     * @return 时间窗口（秒）
     */
    Integer getEnterExitWindow(Long channelId);
}
