package com.vdc.pdi.device.service;

import com.vdc.pdi.device.domain.vo.BoxStatus;
import com.vdc.pdi.device.dto.response.DeviceOverviewResponse;
import com.vdc.pdi.device.dto.response.SiteDeviceStatusResponse;

import java.util.List;

/**
 * 设备状态服务接口
 */
public interface DeviceStatusService {

    /**
     * 获取设备状态概览
     *
     * @param siteId 站点ID
     * @param currentSiteId 当前站点ID
     * @param isSuperAdmin 是否超级管理员
     * @return 设备状态概览
     */
    DeviceOverviewResponse getOverview(Long siteId, Long currentSiteId, boolean isSuperAdmin);

    /**
     * 获取所有站点设备状态统计
     *
     * @param currentSiteId 当前站点ID
     * @param isSuperAdmin 是否超级管理员
     * @return 站点设备状态列表
     */
    List<SiteDeviceStatusResponse> getAllSitesStatus(Long currentSiteId, boolean isSuperAdmin);

    /**
     * 获取盒子在线状态
     *
     * @param boxId 盒子ID
     * @return 盒子状态
     */
    BoxStatus getBoxStatus(Long boxId);
}
