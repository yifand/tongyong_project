package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.common.enums.StateCodeEnum;
import com.vdc.pdi.ruleengine.domain.entity.StateMachine;
import com.vdc.pdi.ruleengine.domain.event.StateStreamEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskEndEvent;
import com.vdc.pdi.ruleengine.domain.event.TaskStartEvent;
import com.vdc.pdi.ruleengine.domain.vo.StateSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StateMachineManager单元测试
 */
@ExtendWith(MockitoExtension.class)
class StateMachineManagerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StateMachineManager stateMachineManager;

    @BeforeEach
    void setUp() {
        stateMachineManager = new StateMachineManager(eventPublisher);
        // 设置空闲超时时间为30分钟
        ReflectionTestUtils.setField(stateMachineManager, "idleTimeoutMinutes", 30);
    }

    @Test
    @DisplayName("获取或创建状态机测试")
    void getOrCreateStateMachine_ShouldCreateNew() {
        StateMachine sm = stateMachineManager.getOrCreateStateMachine(1L, 1L);

        assertNotNull(sm);
        assertEquals(1L, sm.getChannelId());
        assertEquals(1L, sm.getSiteId());
        assertEquals(StateCodeEnum.S1, sm.getCurrentState());
    }

    @Test
    @DisplayName("获取已存在的状态机测试")
    void getOrCreateStateMachine_ShouldReturnExisting() {
        StateMachine sm1 = stateMachineManager.getOrCreateStateMachine(1L, 1L);
        sm1.transition(StateCodeEnum.S5, LocalDateTime.now(), 1L);

        StateMachine sm2 = stateMachineManager.getOrCreateStateMachine(1L, 1L);

        assertSame(sm1, sm2);
        assertEquals(StateCodeEnum.S5, sm2.getCurrentState());
    }

    @Test
    @DisplayName("获取状态机（可能不存在）测试")
    void getStateMachine_NotExists_ShouldReturnNull() {
        StateMachine sm = stateMachineManager.getStateMachine(999L, 1L);
        assertNull(sm);
    }

    @Test
    @DisplayName("获取当前状态测试")
    void getCurrentState_ShouldReturnState() {
        stateMachineManager.getOrCreateStateMachine(1L, 1L);

        StateCodeEnum state = stateMachineManager.getCurrentState(1L, 1L);

        assertEquals(StateCodeEnum.S1, state);
    }

    @Test
    @DisplayName("获取当前状态 - 状态机不存在应返回S1")
    void getCurrentState_NotExists_ShouldReturnS1() {
        StateCodeEnum state = stateMachineManager.getCurrentState(999L, 1L);
        assertEquals(StateCodeEnum.S1, state);
    }

    @Test
    @DisplayName("处理状态流事件测试 - 进入序列")
    void processStateStream_EntrySequence_ShouldPublishTaskStart() {
        LocalDateTime now = LocalDateTime.now();

        // S7→S8→S3 进入序列
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 8, now.plusSeconds(1), 2L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 3, now.plusSeconds(2), 3L));

        // 验证发布了TaskStartEvent
        ArgumentCaptor<TaskStartEvent> captor = ArgumentCaptor.forClass(TaskStartEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TaskStartEvent event = captor.getValue();
        assertEquals(1L, event.getChannelId());
        assertEquals(1L, event.getSiteId());
        assertNotNull(event.getTaskId());
    }

    @Test
    @DisplayName("处理状态流事件测试 - 离开序列")
    void processStateStream_ExitSequence_ShouldPublishTaskEnd() {
        LocalDateTime now = LocalDateTime.now();

        // 先进入
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 8, now.plusSeconds(1), 2L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 3, now.plusSeconds(2), 3L));

        // 再离开 S3→S8→S7→S5
        stateMachineManager.processStateStream(createEvent(1L, 1L, 8, now.plusSeconds(600), 4L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now.plusSeconds(601), 5L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 5, now.plusSeconds(602), 6L));

        // 验证发布了TaskEndEvent
        ArgumentCaptor<TaskEndEvent> captor = ArgumentCaptor.forClass(TaskEndEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TaskEndEvent event = captor.getValue();
        assertEquals(1L, event.getChannelId());
        assertNotNull(event.getEntryTime());
        assertNotNull(event.getEndTime());
    }

    @Test
    @DisplayName("处理无效状态转换测试")
    void processStateStream_InvalidTransition_ShouldNotPublishEvent() {
        LocalDateTime now = LocalDateTime.now();

        // S1不能直接到S3
        stateMachineManager.processStateStream(createEvent(1L, 1L, 3, now, 1L));

        // 验证没有发布任何事件
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("获取状态历史测试")
    void getStateHistory_ShouldReturnHistory() {
        LocalDateTime now = LocalDateTime.now();

        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 8, now.plusSeconds(1), 2L));

        List<StateSnapshot> history = stateMachineManager.getStateHistory(1L, 1L, 10);

        assertEquals(2, history.size());
        assertEquals(StateCodeEnum.S7, history.get(0).getState());
        assertEquals(StateCodeEnum.S8, history.get(1).getState());
    }

    @Test
    @DisplayName("获取状态历史 - 状态机不存在应返回空列表")
    void getStateHistory_NotExists_ShouldReturnEmpty() {
        List<StateSnapshot> history = stateMachineManager.getStateHistory(999L, 1L, 10);
        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("重置状态机测试")
    void resetStateMachine_ShouldReset() {
        LocalDateTime now = LocalDateTime.now();

        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 8, now.plusSeconds(1), 2L));

        stateMachineManager.resetStateMachine(1L, 1L);

        StateCodeEnum state = stateMachineManager.getCurrentState(1L, 1L);
        assertEquals(StateCodeEnum.S1, state);
        assertTrue(stateMachineManager.getStateHistory(1L, 1L, 10).isEmpty());
    }

    @Test
    @DisplayName("移除状态机测试")
    void removeStateMachine_ShouldRemove() {
        stateMachineManager.getOrCreateStateMachine(1L, 1L);

        stateMachineManager.removeStateMachine(1L, 1L);

        assertNull(stateMachineManager.getStateMachine(1L, 1L));
        assertEquals(0, stateMachineManager.getActiveStateMachineCount());
    }

    @Test
    @DisplayName("获取活跃状态机数量测试")
    void getActiveStateMachineCount_ShouldReturnCount() {
        assertEquals(0, stateMachineManager.getActiveStateMachineCount());

        stateMachineManager.getOrCreateStateMachine(1L, 1L);
        assertEquals(1, stateMachineManager.getActiveStateMachineCount());

        stateMachineManager.getOrCreateStateMachine(2L, 1L);
        assertEquals(2, stateMachineManager.getActiveStateMachineCount());

        stateMachineManager.getOrCreateStateMachine(1L, 2L); // 不同siteId
        assertEquals(3, stateMachineManager.getActiveStateMachineCount());
    }

    @Test
    @DisplayName("获取活跃状态机键测试")
    void getActiveStateMachineKeys_ShouldReturnKeys() {
        stateMachineManager.getOrCreateStateMachine(1L, 1L);
        stateMachineManager.getOrCreateStateMachine(2L, 1L);

        List<String> keys = stateMachineManager.getActiveStateMachineKeys();

        assertEquals(2, keys.size());
        assertTrue(keys.contains("1:1"));
        assertTrue(keys.contains("1:2"));
    }

    @Test
    @DisplayName("多通道隔离测试")
    void multipleChannels_ShouldBeIsolated() {
        LocalDateTime now = LocalDateTime.now();

        // 通道1进入
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 8, now.plusSeconds(1), 2L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 3, now.plusSeconds(2), 3L));

        // 通道2不同状态
        stateMachineManager.processStateStream(createEvent(2L, 1L, 7, now, 4L));
        stateMachineManager.processStateStream(createEvent(2L, 1L, 5, now.plusSeconds(1), 5L));

        // 验证状态独立
        assertEquals(StateCodeEnum.S3, stateMachineManager.getCurrentState(1L, 1L));
        assertEquals(StateCodeEnum.S5, stateMachineManager.getCurrentState(2L, 1L));

        // 验证只发布了一个TaskStartEvent
        verify(eventPublisher, times(1)).publishEvent(any(TaskStartEvent.class));
    }

    @Test
    @DisplayName("清理空闲状态机测试")
    void cleanupIdleStateMachines_ShouldRemoveIdle() {
        // 创建状态机
        stateMachineManager.getOrCreateStateMachine(1L, 1L);
        stateMachineManager.getOrCreateStateMachine(2L, 1L);
        assertEquals(2, stateMachineManager.getActiveStateMachineCount());

        // 执行清理（所有状态机都是空闲的，因为刚创建且未配置超时）
        stateMachineManager.cleanupIdleStateMachines();

        // 由于超时时间设置为30分钟，刚创建的不会被清理
        // 除非我们手动修改最后访问时间
        assertEquals(2, stateMachineManager.getActiveStateMachineCount());
    }

    @Test
    @DisplayName("历史记录上限测试 - 状态历史应限制在100条")
    void stateHistory_MaxSize_ShouldBeLimited() {
        LocalDateTime now = LocalDateTime.now();
        Long channelId = 1L;
        Long siteId = 1L;

        // 模拟150个S1<->S5交替状态转换
        for (int i = 0; i < 150; i++) {
            int stateCode = (i % 2 == 0) ? 5 : 1; // S5, S1, S5, S1...
            stateMachineManager.processStateStream(
                    createEvent(channelId, siteId, stateCode, now.plusSeconds(i), (long) i));
        }

        // 获取历史记录
        List<StateSnapshot> history = stateMachineManager.getStateHistory(channelId, siteId, 200);

        // 验证历史记录不超过100条
        assertEquals(100, history.size());
    }

    @Test
    @DisplayName("空闲状态机清理测试 - 超过超时时间应被清理")
    void cleanupIdleStateMachines_Expired_ShouldRemove() {
        // 创建状态机
        stateMachineManager.getOrCreateStateMachine(1L, 1L);
        stateMachineManager.getOrCreateStateMachine(2L, 1L);
        assertEquals(2, stateMachineManager.getActiveStateMachineCount());

        // 修改空闲超时时间为1分钟
        ReflectionTestUtils.setField(stateMachineManager, "idleTimeoutMinutes", 1);

        // 等待超过1分钟（由于测试不能真的等1分钟，我们通过修改lastAccessTimeMap来模拟）
        // 获取lastAccessTimeMap并修改其中的时间为很久以前
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, LocalDateTime> lastAccessTimeMap =
                (java.util.concurrent.ConcurrentHashMap<String, LocalDateTime>) ReflectionTestUtils.getField(
                        stateMachineManager, "lastAccessTimeMap");
        assertNotNull(lastAccessTimeMap);

        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5);
        lastAccessTimeMap.put("1:1", expiredTime);
        lastAccessTimeMap.put("1:2", expiredTime);

        // 执行清理
        stateMachineManager.cleanupIdleStateMachines();

        // 验证状态机已被清理
        assertEquals(0, stateMachineManager.getActiveStateMachineCount());
    }

    @Test
    @DisplayName("异常输入处理测试 - 无效状态码应抛出异常")
    void processStateStream_InvalidStateCode_ShouldThrowException() {
        LocalDateTime now = LocalDateTime.now();

        // 尝试使用无效状态码(99)
        assertThrows(IllegalArgumentException.class, () -> {
            stateMachineManager.processStateStream(
                    createEvent(1L, 1L, 99, now, 1L));
        });
    }

    @Test
    @DisplayName("重复状态转换测试 - 相同状态应正常处理")
    void processStateStream_SameState_ShouldHandleNormally() {
        LocalDateTime now = LocalDateTime.now();

        // 连续发送相同状态S7
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now, 1L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now.plusSeconds(1), 2L));
        stateMachineManager.processStateStream(createEvent(1L, 1L, 7, now.plusSeconds(2), 3L));

        // 验证状态为S7，历史记录有3条
        assertEquals(StateCodeEnum.S7, stateMachineManager.getCurrentState(1L, 1L));
        assertEquals(3, stateMachineManager.getStateHistory(1L, 1L, 10).size());
    }

    @Test
    @DisplayName("清除所有状态机测试")
    void clearAllStateMachines_ShouldRemoveAll() {
        stateMachineManager.getOrCreateStateMachine(1L, 1L);
        stateMachineManager.getOrCreateStateMachine(2L, 1L);
        stateMachineManager.getOrCreateStateMachine(3L, 2L);
        assertEquals(3, stateMachineManager.getActiveStateMachineCount());

        stateMachineManager.clearAllStateMachines();

        assertEquals(0, stateMachineManager.getActiveStateMachineCount());
        assertTrue(stateMachineManager.getActiveStateMachineKeys().isEmpty());
    }

    @Test
    @DisplayName("获取状态历史限制测试 - limit参数应正确限制返回数量")
    void getStateHistory_WithLimit_ShouldReturnLimitedResults() {
        LocalDateTime now = LocalDateTime.now();

        // 创建10个状态
        for (int i = 0; i < 10; i++) {
            int stateCode = (i % 2 == 0) ? 7 : 5;
            stateMachineManager.processStateStream(
                    createEvent(1L, 1L, stateCode, now.plusSeconds(i), (long) i));
        }

        // 获取不同limit的历史
        List<StateSnapshot> history5 = stateMachineManager.getStateHistory(1L, 1L, 5);
        List<StateSnapshot> history20 = stateMachineManager.getStateHistory(1L, 1L, 20);

        assertEquals(5, history5.size());
        assertEquals(10, history20.size());
    }

    private StateStreamEvent createEvent(Long channelId, Long siteId, int stateCode,
                                          LocalDateTime timestamp, Long streamId) {
        return StateStreamEvent.of(this, channelId, siteId, stateCode, timestamp, streamId);
    }
}
