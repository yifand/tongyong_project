package com.vdc.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vdc.platform.entity.WorkSession;

public interface IWorkSessionService extends IService<WorkSession> {

    /**
     * 计算并更新 WorkSession 的 actual_duration、deviation_pct 和 result。
     *
     * @param session 待计算的 WorkSession
     * @return 计算后的 WorkSession
     */
    WorkSession calculateMetrics(WorkSession session);
}
