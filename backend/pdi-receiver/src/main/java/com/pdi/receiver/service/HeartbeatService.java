package com.pdi.receiver.service;

import com.pdi.receiver.dto.HeartbeatDTO;

/**
 * 心跳处理服务
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface HeartbeatService {

    /**
     * 处理心跳
     *
     * @param dto 心跳DTO
     */
    void processHeartbeat(HeartbeatDTO dto);

    /**
     * 检查盒子心跳超时
     * 定时任务调用
     */
    void checkHeartbeatTimeout();

    /**
     * 更新盒子状态为在线
     *
     * @param boxId 盒子ID
     */
    void markBoxOnline(Long boxId);

    /**
     * 更新盒子状态为离线
     *
     * @param boxId 盒子ID
     */
    void markBoxOffline(Long boxId);

}
