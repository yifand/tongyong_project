package com.pdi.service.device;

import com.pdi.common.result.PageResult;
import com.pdi.service.device.dto.BoxDTO;
import com.pdi.service.device.dto.BoxQueryDTO;
import com.pdi.service.device.dto.ChannelDTO;

import java.util.List;

/**
 * 盒子设备服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface BoxService {

    /**
     * 创建盒子
     *
     * @param dto 盒子信息
     * @return 创建后的盒子信息
     */
    BoxDTO createBox(BoxDTO dto);

    /**
     * 更新盒子
     *
     * @param boxId 盒子ID
     * @param dto   盒子信息
     */
    void updateBox(Long boxId, BoxDTO dto);

    /**
     * 删除盒子
     *
     * @param boxId 盒子ID
     */
    void deleteBox(Long boxId);

    /**
     * 获取盒子详情
     *
     * @param boxId 盒子ID
     * @return 盒子详情
     */
    BoxDTO getBox(Long boxId);

    /**
     * 分页查询盒子列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<BoxDTO> listBoxes(BoxQueryDTO query);

    /**
     * 更新盒子状态
     *
     * @param boxId  盒子ID
     * @param status 状态: 0-离线, 1-在线, 2-故障
     */
    void updateBoxStatus(Long boxId, Integer status);

    /**
     * 远程重启盒子
     *
     * @param boxId 盒子ID
     * @return 命令ID
     */
    String rebootBox(Long boxId);

    /**
     * 获取盒子资源使用率
     *
     * @param boxId 盒子ID
     * @return 资源使用率信息
     */
    BoxDTO getBoxMetrics(Long boxId);

    /**
     * 处理心跳
     *
     * @param boxId 盒子ID
     * @param dto   心跳信息
     */
    void handleHeartbeat(Long boxId, BoxDTO dto);

    // ==================== 通道管理 ====================

    /**
     * 创建通道
     *
     * @param dto 通道信息
     * @return 创建后的通道信息
     */
    ChannelDTO createChannel(ChannelDTO dto);

    /**
     * 更新通道
     *
     * @param channelId 通道ID
     * @param dto       通道信息
     */
    void updateChannel(Long channelId, ChannelDTO dto);

    /**
     * 删除通道
     *
     * @param channelId 通道ID
     */
    void deleteChannel(Long channelId);

    /**
     * 获取通道详情
     *
     * @param channelId 通道ID
     * @return 通道详情
     */
    ChannelDTO getChannel(Long channelId);

    /**
     * 分页查询通道列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<ChannelDTO> listChannels(BoxQueryDTO query);

    /**
     * 获取盒子的所有通道
     *
     * @param boxId 盒子ID
     * @return 通道列表
     */
    List<ChannelDTO> listChannelsByBoxId(Long boxId);

    /**
     * 更新通道状态
     *
     * @param channelId 通道ID
     * @param status    状态: 0-禁用, 1-启用, 2-故障
     */
    void updateChannelStatus(Long channelId, Integer status);

    /**
     * 更新通道算法配置
     *
     * @param channelId 通道ID
     * @param algorithmConfig 算法配置JSON
     */
    void updateChannelAlgorithm(Long channelId, String algorithmConfig);

}
