package com.vdc.pdi.alarm.service;

import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE服务接口
 */
public interface AlarmSSEService {

    /**
     * 建立SSE订阅连接
     */
    SseEmitter subscribe();

    /**
     * 推送报警给所有订阅者
     */
    void pushAlarm(AlarmResponse alarm);

    /**
     * 关闭指定连接
     */
    void closeEmitter(String emitterId);
}
