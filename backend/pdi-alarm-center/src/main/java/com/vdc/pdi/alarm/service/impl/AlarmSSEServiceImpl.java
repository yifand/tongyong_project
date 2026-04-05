package com.vdc.pdi.alarm.service.impl;

import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.service.AlarmSSEService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE服务实现
 */
@Service
@Slf4j
public class AlarmSSEServiceImpl implements AlarmSSEService {

    /**
     * SSE连接超时时间：30分钟
     */
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 心跳间隔：30秒
     */
    private static final long HEARTBEAT_INTERVAL = 30 * 1000L;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    @jakarta.annotation.PostConstruct
    public void init() {
        // 启动心跳任务
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat,
                HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @jakarta.annotation.PreDestroy
    public void destroy() {
        heartbeatExecutor.shutdown();
        emitters.forEach((id, emitter) -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("关闭SSE连接失败: {}", id);
            }
        });
        emitters.clear();
    }

    @Override
    public SseEmitter subscribe() {
        String emitterId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 存储连接
        emitters.put(emitterId, emitter);

        // 连接关闭时清理
        emitter.onCompletion(() -> {
            log.debug("SSE连接完成: {}", emitterId);
            emitters.remove(emitterId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE连接超时: {}", emitterId);
            emitters.remove(emitterId);
        });

        emitter.onError(e -> {
            log.error("SSE连接错误: {}", emitterId, e);
            emitters.remove(emitterId);
        });

        // 发送连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data(Map.of("emitterId", emitterId, "time", System.currentTimeMillis())));
        } catch (IOException e) {
            log.error("发送SSE连接事件失败", e);
            emitters.remove(emitterId);
        }

        log.info("SSE连接建立: {}, 当前连接数: {}", emitterId, emitters.size());
        return emitter;
    }

    @Override
    public void pushAlarm(AlarmResponse alarm) {
        if (emitters.isEmpty()) {
            return;
        }

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("alarm")
                .id(alarm.getId().toString())
                .data(alarm);

        // 广播给所有订阅者
        List<String> deadEmitters = new ArrayList<>();
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(event);
            } catch (Exception e) {
                log.warn("推送报警失败，标记连接待清理: {}", id);
                deadEmitters.add(id);
            }
        });

        // 清理失效连接
        deadEmitters.forEach(emitters::remove);

        log.debug("报警推送完成: alarmId={}, 活跃连接数={}", alarm.getId(), emitters.size());
    }

    @Override
    public void closeEmitter(String emitterId) {
        SseEmitter emitter = emitters.remove(emitterId);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE连接手动关闭: {}", emitterId);
        }
    }

    /**
     * 发送心跳保持连接
     */
    private void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        List<String> deadEmitters = new ArrayList<>();
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data(Map.of("time", System.currentTimeMillis())));
            } catch (Exception e) {
                deadEmitters.add(id);
            }
        });

        deadEmitters.forEach(emitters::remove);

        if (!deadEmitters.isEmpty()) {
            log.debug("心跳检测清理失效连接: {}, 剩余连接数: {}", deadEmitters.size(), emitters.size());
        }
    }
}
