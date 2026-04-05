package com.vdc.pdi.algorithminlet.service;

import com.vdc.pdi.algorithminlet.dto.request.HeartbeatRequest;

/**
 * 心跳服务接口
 */
public interface HeartbeatService {

    /**
     * 处理心跳数据
     *
     * @param request 心跳请求
     * @param siteId  站点ID
     */
    void processHeartbeat(HeartbeatRequest request, Long siteId);
}
