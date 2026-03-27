package com.pdi.service.device;

import com.pdi.common.result.PageResult;
import com.pdi.service.device.dto.SiteDTO;

import java.util.List;

/**
 * 监测点位服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface SiteService {

    /**
     * 创建点位
     *
     * @param dto 点位信息
     * @return 创建后的点位信息
     */
    SiteDTO createSite(SiteDTO dto);

    /**
     * 更新点位
     *
     * @param siteId 点位ID
     * @param dto    点位信息
     */
    void updateSite(Long siteId, SiteDTO dto);

    /**
     * 删除点位
     *
     * @param siteId 点位ID
     */
    void deleteSite(Long siteId);

    /**
     * 获取点位详情
     *
     * @param siteId 点位ID
     * @return 点位详情
     */
    SiteDTO getSite(Long siteId);

    /**
     * 获取所有点位列表
     *
     * @return 点位列表
     */
    List<SiteDTO> listAllSites();

    /**
     * 分页查询点位列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<SiteDTO> listSites(Long page, Long size);

    /**
     * 更新点位状态
     *
     * @param siteId 点位ID
     * @param status 状态: 0-禁用, 1-启用
     */
    void updateSiteStatus(Long siteId, Integer status);

}
