package com.vdc.pdi.device.service;

import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.dto.request.ChannelQueryRequest;
import com.vdc.pdi.device.dto.request.ChannelRequest;
import com.vdc.pdi.device.dto.response.ChannelResponse;

import java.util.List;

/**
 * 通道服务接口
 */
public interface ChannelService {

    /**
     * 分页查询通道列表
     *
     * @param request 查询请求
     * @param currentSiteId 当前站点ID
     * @return 分页结果
     */
    PageResponse<ChannelResponse> listChannels(ChannelQueryRequest request, Long currentSiteId);

    /**
     * 获取通道详情
     *
     * @param id 通道ID
     * @param currentSiteId 当前站点ID
     * @return 通道详情
     */
    ChannelResponse getChannel(Long id, Long currentSiteId);

    /**
     * 创建通道
     *
     * @param request 创建请求
     * @param currentSiteId 当前站点ID
     * @param currentUserId 当前用户ID
     * @return 通道ID
     */
    Long createChannel(ChannelRequest request, Long currentSiteId, Long currentUserId);

    /**
     * 更新通道
     *
     * @param id 通道ID
     * @param request 更新请求
     * @param currentSiteId 当前站点ID
     */
    void updateChannel(Long id, ChannelRequest request, Long currentSiteId);

    /**
     * 删除通道
     *
     * @param id 通道ID
     * @param currentSiteId 当前站点ID
     */
    void deleteChannel(Long id, Long currentSiteId);

    /**
     * 根据盒子ID查询通道列表
     *
     * @param boxId 盒子ID
     * @param currentSiteId 当前站点ID
     * @return 通道列表
     */
    List<ChannelResponse> getChannelsByBoxId(Long boxId, Long currentSiteId);

    /**
     * 根据ID查询通道（内部调用）
     *
     * @param id 通道ID
     * @return 通道实体
     */
    Channel getChannelById(Long id);

    /**
     * 根据盒子ID查询通道列表（内部调用）
     *
     * @param boxId 盒子ID
     * @return 通道实体列表
     */
    List<Channel> getChannelsByBoxIdInternal(Long boxId);

    /**
     * 更新通道状态
     *
     * @param channelId 通道ID
     * @param status 状态
     */
    void updateChannelStatus(Long channelId, Integer status);
}
