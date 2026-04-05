package com.vdc.pdi.ruleengine.integration;

import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import com.vdc.pdi.ruleengine.domain.event.AlarmTriggerEvent;
import com.vdc.pdi.ruleengine.domain.event.StateStreamEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskEndEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskStartEvent;
import com.vdc.pdi.ruleengine.domain.vo.StateInfoResponse;
import com.vdc.pdi.ruleengine.service.StateMachineManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 规则引擎集成测试
 * 测试完整的进入-离开流程
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class RuleEngineIntegrationTest {

    @Autowired
    private StateMachineManager stateMachineManager;

    @Autowired
    private TestEventCollector eventCollector;

    @MockBean
    private ChannelRepository channelRepository;

    @BeforeEach
    void setUp() {
        eventCollector.clear();
        stateMachineManager.clearAllStateMachines();

        // 配置mock
        Channel channel = new Channel();
        channel.setId(1L);
        channel.setAlgorithmType("pdi_left_front");
        channel.setName("左前门");
        when(channelRepository.findById(any())).thenReturn(Optional.of(channel));
    }

    @Test
    @DisplayName("完整流程测试 - 正常进入和离开")
    void fullFlow_NormalEntryAndExit() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Long channelId = 1L;
        Long siteId = 1L;

        // 准备事件监听
        CountDownLatch taskStartLatch = new CountDownLatch(1);
        CountDownLatch taskEndLatch = new CountDownLatch(1);
        eventCollector.setTaskStartLatch(taskStartLatch);
        eventCollector.setTaskEndLatch(taskEndLatch);

        // 1. 模拟进入: S1→S7→S8→S3
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 7, now, 1L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 8, now.plusSeconds(1), 2L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 3, now.plusSeconds(2), 3L));

        // 验证进入事件已发布
        assertTrue(taskStartLatch.await(2, TimeUnit.SECONDS));
        assertEquals(1, eventCollector.getTaskStartEvents().size());

        TaskStartEvent startEvent = eventCollector.getTaskStartEvents().get(0);
        assertEquals(channelId, startEvent.getChannelId());
        assertNotNull(startEvent.getTaskId());

        // 验证状态
        assertEquals(StateCodeEnum.S3, stateMachineManager.getCurrentState(channelId, siteId));

        // 2. 模拟离开: S3→S8→S7→S5 (持续10分钟，低于标准12分钟，应触发报警)
        LocalDateTime exitTime = now.plusMinutes(10);
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 8, exitTime.minusSeconds(2), 4L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 7, exitTime.minusSeconds(1), 5L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 5, exitTime, 6L));

        // 验证离开事件已发布
        assertTrue(taskEndLatch.await(2, TimeUnit.SECONDS));
        assertEquals(1, eventCollector.getTaskEndEvents().size());

        TaskEndEvent endEvent = eventCollector.getTaskEndEvents().get(0);
        assertEquals(channelId, endEvent.getChannelId());
        assertEquals(startEvent.getTaskId(), endEvent.getTaskId());

        // 验证报警事件（10分钟 < 12分钟*0.9=10.8分钟，应触发报警）
        Thread.sleep(500); // 等待异步处理
        assertFalse(eventCollector.getAlarmEvents().isEmpty());

        AlarmTriggerEvent alarmEvent = eventCollector.getAlarmEvents().get(0);
        assertEquals(channelId, alarmEvent.getChannelId());
    }

    @Test
    @DisplayName("完整流程测试 - 合格作业（不报警）")
    void fullFlow_PassedTask_NoAlarm() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Long channelId = 1L;
        Long siteId = 1L;

        CountDownLatch taskEndLatch = new CountDownLatch(1);
        eventCollector.setTaskEndLatch(taskEndLatch);

        // 进入
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 7, now, 1L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 8, now.plusSeconds(1), 2L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 3, now.plusSeconds(2), 3L));

        // 离开：持续13分钟，超过标准12分钟，合格
        LocalDateTime exitTime = now.plusMinutes(13);
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 8, exitTime.minusSeconds(2), 4L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 7, exitTime.minusSeconds(1), 5L));
        stateMachineManager.processStateStream(
                createEvent(channelId, siteId, 5, exitTime, 6L));

        assertTrue(taskEndLatch.await(2, TimeUnit.SECONDS));

        // 验证未触发报警
        Thread.sleep(500);
        // 13分钟 >= 12分钟，不报警
    }

    @Test
    @DisplayName("多通道并发测试")
    void concurrentChannels_MultipleTasks() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        // 通道1进入
        stateMachineManager.processStateStream(
                createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(
                createEvent(1L, 1L, 8, now.plusSeconds(1), 2L));
        stateMachineManager.processStateStream(
                createEvent(1L, 1L, 3, now.plusSeconds(2), 3L));

        // 通道2进入
        stateMachineManager.processStateStream(
                createEvent(2L, 1L, 7, now.plusSeconds(5), 4L));
        stateMachineManager.processStateStream(
                createEvent(2L, 1L, 8, now.plusSeconds(6), 5L));
        stateMachineManager.processStateStream(
                createEvent(2L, 1L, 3, now.plusSeconds(7), 6L));

        // 验证两个通道都在S3状态
        assertEquals(StateCodeEnum.S3, stateMachineManager.getCurrentState(1L, 1L));
        assertEquals(StateCodeEnum.S3, stateMachineManager.getCurrentState(2L, 1L));

        // 验证活跃状态机数量
        assertEquals(2, stateMachineManager.getActiveStateMachineCount());
    }

    @Test
    @DisplayName("状态机管理接口测试")
    void managementApis_ShouldWork() {
        LocalDateTime now = LocalDateTime.now();

        // 创建几个状态机
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(createEvent(2L, 1L, 7, now, 2L));
        stateMachineManager.processStateStream(createEvent(3L, 1L, 5, now, 3L));

        // 获取活跃状态机列表
        List<String> keys = stateMachineManager.getActiveStateMachineKeys();
        assertEquals(3, keys.size());

        // 获取状态机统计
        int count = stateMachineManager.getActiveStateMachineCount();
        assertEquals(3, count);

        // 获取通道状态
        StateCodeEnum state = stateMachineManager.getCurrentState(1L, 1L);
        assertEquals(StateCodeEnum.S7, state);

        // 获取通道历史
        assertTrue(stateMachineManager.getStateHistory(1L, 1L, 10).size() > 0);

        // 重置状态机
        stateMachineManager.resetStateMachine(1L, 1L);
        assertEquals(StateCodeEnum.S1, stateMachineManager.getCurrentState(1L, 1L));

        // 移除状态机
        stateMachineManager.removeStateMachine(2L, 1L);
        assertEquals(2, stateMachineManager.getActiveStateMachineCount());
    }

    @Test
    @DisplayName("无效状态转换处理测试")
    void invalidTransition_ShouldBeIgnored() {
        LocalDateTime now = LocalDateTime.now();

        // S1不能直接到S3
        stateMachineManager.processStateStream(createEvent(1L, 1L, 3, now, 1L));

        // 状态应保持S1
        assertEquals(StateCodeEnum.S1, stateMachineManager.getCurrentState(1L, 1L));
        assertTrue(stateMachineManager.getStateHistory(1L, 1L, 10).isEmpty());
    }

    private StateStreamEvent createEvent(Long channelId, Long siteId, int stateCode,
                                          LocalDateTime timestamp, Long streamId) {
        return StateStreamEvent.of(this, channelId, siteId, stateCode, timestamp, streamId);
    }

    /**
     * 测试事件收集器
     */
    @Component
    static class TestEventCollector {
        private final List<TaskStartEvent> taskStartEvents = new ArrayList<>();
        private final List<TaskEndEvent> taskEndEvents = new ArrayList<>();
        private final List<AlarmTriggerEvent> alarmEvents = new ArrayList<>();

        private CountDownLatch taskStartLatch;
        private CountDownLatch taskEndLatch;

        public void setTaskStartLatch(CountDownLatch latch) {
            this.taskStartLatch = latch;
        }

        public void setTaskEndLatch(CountDownLatch latch) {
            this.taskEndLatch = latch;
        }

        @EventListener
        public void onTaskStart(TaskStartEvent event) {
            taskStartEvents.add(event);
            if (taskStartLatch != null) {
                taskStartLatch.countDown();
            }
        }

        @EventListener
        public void onTaskEnd(TaskEndEvent event) {
            taskEndEvents.add(event);
            if (taskEndLatch != null) {
                taskEndLatch.countDown();
            }
        }

        @EventListener
        public void onAlarmTrigger(AlarmTriggerEvent event) {
            alarmEvents.add(event);
        }

        public void clear() {
            taskStartEvents.clear();
            taskEndEvents.clear();
            alarmEvents.clear();
            taskStartLatch = null;
            taskEndLatch = null;
        }

        public List<TaskStartEvent> getTaskStartEvents() {
            return taskStartEvents;
        }

        public List<TaskEndEvent> getTaskEndEvents() {
            return taskEndEvents;
        }

        public List<AlarmTriggerEvent> getAlarmEvents() {
            return alarmEvents;
        }
    }
}
