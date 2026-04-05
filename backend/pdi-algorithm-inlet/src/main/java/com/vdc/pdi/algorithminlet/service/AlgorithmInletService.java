package com.vdc.pdi.algorithminlet.service;

import com.vdc.pdi.algorithminlet.dto.request.AlarmEventRequest;
import com.vdc.pdi.algorithminlet.dto.request.StateStreamRequest;

/**
 * 算法数据入口服务接口
 */
public interface AlgorithmInletService {

    /**
     * 处理状态流数据
     *
     * @param request 状态流请求
     * @param siteId  站点ID
     */
    void processStateStream(StateStreamRequest request, Long siteId);

    /**
     * 处理报警事件
     *
     * @param request 报警事件请求
     * @param siteId  站点ID
     */
    void processAlarmEvent(AlarmEventRequest request, Long siteId);
}
