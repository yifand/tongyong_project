package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.ruleengine.domain.vo.SingleTaskInfo;
import com.vdc.pdi.ruleengine.domain.vo.TaskDurationResult;
import com.vdc.pdi.ruleengine.exception.RuleEngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PdiTaskCalculator单元测试
 */
class PdiTaskCalculatorTest {

    private PdiTaskCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PdiTaskCalculator();
        // 使用反射设置配置值（模拟@Value注入）
        ReflectionTestUtils.setField(calculator, "standardMinutes", 12);
        ReflectionTestUtils.setField(calculator, "vehicleStandardMinutes", 15);
    }

    @Test
    @DisplayName("单通道计算测试 - 基本场景")
    void calculate_SingleChannel() {
        // 准备
        Long channelId = 1L;
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.plusMinutes(10); // 10分钟，标准12分钟

        // 执行
        TaskDurationResult result = calculator.calculate(channelId, entryTime, exitTime);

        // 验证
        assertEquals(channelId, result.getChannelId());
        assertFalse(result.isMerged());
        assertEquals(600, result.getActualSeconds()); // 10分钟 = 600秒
        assertEquals(720, result.getStandardSeconds()); // 12分钟 = 720秒
        assertEquals(-120, result.getDeviationSeconds()); // 偏差 -120秒
        assertEquals("10分0秒", result.getFormattedActualDuration());
        assertEquals("-2分0秒", result.getFormattedDeviation());
    }

    @Test
    @DisplayName("单通道计算测试 - 超过标准时长")
    void calculate_ExceedStandard() {
        Long channelId = 1L;
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.plusMinutes(15); // 15分钟

        TaskDurationResult result = calculator.calculate(channelId, entryTime, exitTime);

        assertEquals(900, result.getActualSeconds()); // 15分钟 = 900秒
        assertEquals(180, result.getDeviationSeconds()); // 偏差 +180秒
        assertEquals("+3分0秒", result.getFormattedDeviation());
    }

    @Test
    @DisplayName("单通道计算测试 - entryTime为空应抛出异常")
    void calculate_NullEntryTime_ShouldThrow() {
        LocalDateTime exitTime = LocalDateTime.now();

        assertThrows(RuleEngineException.class, () ->
                calculator.calculate(1L, null, exitTime));
    }

    @Test
    @DisplayName("单通道计算测试 - exitTime为空应抛出异常")
    void calculate_NullExitTime_ShouldThrow() {
        LocalDateTime entryTime = LocalDateTime.now();

        assertThrows(RuleEngineException.class, () ->
                calculator.calculate(1L, entryTime, null));
    }

    @Test
    @DisplayName("单通道计算测试 - exitTime早于entryTime应抛出异常")
    void calculate_ExitBeforeEntry_ShouldThrow() {
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.minusMinutes(5);

        assertThrows(RuleEngineException.class, () ->
                calculator.calculate(1L, entryTime, exitTime));
    }

    @Test
    @DisplayName("多车门合并计算测试 - 基本场景")
    void calculateMerged_MultipleDoors() {
        LocalDateTime baseTime = LocalDateTime.now();

        // 三个车门的作业时间
        // 左前门: 10:00-10:12
        // 左后门: 10:02-10:10
        // 滑门: 10:01-10:11
        // 总时长 = 10:12 - 10:00 = 12分钟
        List<SingleTaskInfo> tasks = List.of(
                SingleTaskInfo.builder()
                        .channelId(1L)
                        .channelName("左前门")
                        .entryTime(baseTime)
                        .exitTime(baseTime.plusMinutes(12))
                        .build(),
                SingleTaskInfo.builder()
                        .channelId(2L)
                        .channelName("左后门")
                        .entryTime(baseTime.plusMinutes(2))
                        .exitTime(baseTime.plusMinutes(10))
                        .build(),
                SingleTaskInfo.builder()
                        .channelId(3L)
                        .channelName("滑门")
                        .entryTime(baseTime.plusMinutes(1))
                        .exitTime(baseTime.plusMinutes(11))
                        .build()
        );

        TaskDurationResult result = calculator.calculateMerged(tasks);

        assertTrue(result.isMerged());
        assertEquals(baseTime, result.getEntryTime()); // 最早进入时间
        assertEquals(baseTime.plusMinutes(12), result.getExitTime()); // 最晚离开时间
        assertEquals(720, result.getActualSeconds()); // 12分钟 = 720秒
        assertEquals(900, result.getStandardSeconds()); // 15分钟 = 900秒
        assertEquals(3, result.getSubTasks().size());
    }

    @Test
    @DisplayName("多车门合并计算测试 - 空列表应抛出异常")
    void calculateMerged_EmptyList_ShouldThrow() {
        assertThrows(RuleEngineException.class, () ->
                calculator.calculateMerged(List.of()));
    }

    @Test
    @DisplayName("多车门合并计算测试 - null列表应抛出异常")
    void calculateMerged_NullList_ShouldThrow() {
        assertThrows(RuleEngineException.class, () ->
                calculator.calculateMerged(null));
    }

    @Test
    @DisplayName("多车门合并计算测试 - 缺失时间应抛出异常")
    void calculateMerged_MissingTime_ShouldThrow() {
        List<SingleTaskInfo> tasks = List.of(
                SingleTaskInfo.builder()
                        .channelId(1L)
                        .entryTime(LocalDateTime.now())
                        .exitTime(null)  // 缺少离开时间
                        .build()
        );

        assertThrows(RuleEngineException.class, () ->
                calculator.calculateMerged(tasks));
    }

    @Test
    @DisplayName("偏差百分比计算测试")
    void calculateDeviationPercentage() {
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(600) // 10分钟
                .standardSeconds(720) // 12分钟
                .deviationSeconds(-120)
                .build();

        double percentage = calculator.calculateDeviationPercentage(result);

        assertEquals(-16.67, percentage, 0.01); // -120/720*100 = -16.67%
    }

    @Test
    @DisplayName("偏差百分比计算测试 - 标准时长为0")
    void calculateDeviationPercentage_ZeroStandard() {
        TaskDurationResult result = TaskDurationResult.builder()
                .standardSeconds(0)
                .build();

        double percentage = calculator.calculateDeviationPercentage(result);

        assertEquals(0.0, percentage);
    }

    @Test
    @DisplayName("格式化输出测试")
    void formattedOutput() {
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(125) // 2分5秒
                .deviationSeconds(-65) // -1分5秒
                .build();

        assertEquals("2分5秒", result.getFormattedActualDuration());
        assertEquals("-1分5秒", result.getFormattedDeviation());
    }

    @Test
    @DisplayName("不同配置标准时长测试")
    void calculate_DifferentConfigValues() {
        // 测试单通道配置 10分钟
        ReflectionTestUtils.setField(calculator, "standardMinutes", 10);

        Long channelId = 1L;
        LocalDateTime entryTime = LocalDateTime.now();
        LocalDateTime exitTime = entryTime.plusMinutes(10);

        TaskDurationResult result1 = calculator.calculate(channelId, entryTime, exitTime);
        assertEquals(600, result1.getStandardSeconds()); // 10分钟

        // 测试车辆配置 20分钟
        ReflectionTestUtils.setField(calculator, "vehicleStandardMinutes", 20);

        List<SingleTaskInfo> tasks = List.of(
                SingleTaskInfo.builder()
                        .channelId(1L)
                        .entryTime(entryTime)
                        .exitTime(exitTime)
                        .build()
        );

        TaskDurationResult result2 = calculator.calculateMerged(tasks);
        assertEquals(1200, result2.getStandardSeconds()); // 20分钟 = 1200秒
    }
}
