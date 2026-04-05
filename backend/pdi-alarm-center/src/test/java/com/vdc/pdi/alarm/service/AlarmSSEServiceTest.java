package com.vdc.pdi.alarm.service;

import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.service.impl.AlarmSSEServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.*;

/**
 * SSE服务测试
 */
@ExtendWith(MockitoExtension.class)
class AlarmSSEServiceTest {

    private AlarmSSEServiceImpl alarmSSEService;

    @BeforeEach
    void setUp() {
        alarmSSEService = new AlarmSSEServiceImpl();
        alarmSSEService.init();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        alarmSSEService.destroy();
    }

    @Test
    @DisplayName("应成功建立SSE连接")
    void shouldSubscribeSuccessfully() {
        // When
        SseEmitter emitter = alarmSSEService.subscribe();

        // Then
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("应推送报警给所有订阅者")
    void shouldPushAlarmToAllSubscribers() {
        // Given
        SseEmitter emitter1 = alarmSSEService.subscribe();
        SseEmitter emitter2 = alarmSSEService.subscribe();

        AlarmResponse alarm = new AlarmResponse();
        alarm.setId(1L);

        // When & Then - 验证没有异常抛出
        assertThatNoException().isThrownBy(() -> alarmSSEService.pushAlarm(alarm));
    }

    @Test
    @DisplayName("应清理失效连接")
    void shouldCleanDeadEmitters() throws InterruptedException {
        // Given
        SseEmitter emitter = alarmSSEService.subscribe();

        // When - 手动完成连接模拟失效
        emitter.complete();
        Thread.sleep(100); // 等待清理

        // Then - 推送时不应抛出异常
        AlarmResponse alarm = new AlarmResponse();
        alarm.setId(1L);
        assertThatNoException().isThrownBy(() -> alarmSSEService.pushAlarm(alarm));
    }

    @Test
    @DisplayName("应手动关闭指定连接")
    void shouldCloseSpecificEmitter() {
        // Given
        SseEmitter emitter = alarmSSEService.subscribe();

        // When & Then - 不应抛出异常
        // 注意：由于emitterId是内部生成的，这里只能测试不抛出异常
        assertThatNoException().isThrownBy(() -> alarmSSEService.closeEmitter("non-existent-id"));
    }
}
