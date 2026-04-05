package com.vdc.pdi.device.service;

import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.vo.BoxStatus;
import com.vdc.pdi.device.domain.vo.HeartbeatInfo;
import com.vdc.pdi.device.dto.request.BoxQueryRequest;
import com.vdc.pdi.device.dto.request.BoxRequest;
import com.vdc.pdi.device.dto.response.BoxResponse;
import com.vdc.pdi.device.dto.response.DeviceMetricsResponse;

/**
 * 盒子服务接口
 */
public interface BoxService {

    /**
     * 分页查询盒子列表
     *
     * @param request 查询请求
     * @param currentSiteId 当前站点ID
     * @return 分页结果
     */
    PageResponse<BoxResponse> listBoxes(BoxQueryRequest request, Long currentSiteId);

    /**
     * 获取盒子详情
     *
     * @param id 盒子ID
     * @param currentSiteId 当前站点ID
     * @return 盒子详情
     */
    BoxResponse getBox(Long id, Long currentSiteId);

    /**
     * 创建盒子
     *
     * @param request 创建请求
     * @param currentSiteId 当前站点ID
     * @param currentUserId 当前用户ID
     * @return 盒子ID
     */
    Long createBox(BoxRequest request, Long currentSiteId, Long currentUserId);

    /**
     * 更新盒子
     *
     * @param id 盒子ID
     * @param request 更新请求
     * @param currentSiteId 当前站点ID
     */
    void updateBox(Long id, BoxRequest request, Long currentSiteId);

    /**
     * 删除盒子
     *
     * @param id 盒子ID
     * @param currentSiteId 当前站点ID
     */
    void deleteBox(Long id, Long currentSiteId);

    /**
     * 远程重启盒子（预留）
     *
     * @param id 盒子ID
     * @param currentSiteId 当前站点ID
     */
    void rebootBox(Long id, Long currentSiteId);

    /**
     * 获取盒子资源使用率
     *
     * @param id 盒子ID
     * @param currentSiteId 当前站点ID
     * @return 资源使用率
     */
    DeviceMetricsResponse getBoxMetrics(Long id, Long currentSiteId);

    /**
     * 根据ID查询盒子（内部调用）
     *
     * @param id 盒子ID
     * @return 盒子实体
     */
    EdgeBox getBoxById(Long id);

    /**
     * 更新盒子心跳信息
     *
     * @param boxId 盒子ID
     * @param heartbeat 心跳信息
     */
    void updateHeartbeat(Long boxId, HeartbeatInfo heartbeat);

    /**
     * 检查并更新离线状态（定时任务调用）
     */
    void checkAndUpdateOfflineStatus();

    /**
     * 获取盒子在线状态
     *
     * @param boxId 盒子ID
     * @return 盒子状态
     */
    BoxStatus getBoxStatus(Long boxId);
}
