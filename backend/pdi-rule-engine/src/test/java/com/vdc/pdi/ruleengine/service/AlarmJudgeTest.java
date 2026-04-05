package com.vdc.pdi.ruleengine.service;

import com.vdc.pdi.common.enums.AlarmTypeEnum;
import com.vdc.pdi.ruleengine.domain.vo.AlarmJudgeResult;
import com.vdc.pdi.ruleengine.domain.vo.ComplianceLevel;
import com.vdc.pdi.ruleengine.domain.vo.TaskDurationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlarmJudge单元测试
 */
class AlarmJudgeTest {

    private AlarmJudge alarmJudge;

    @BeforeEach
    void setUp() {
        alarmJudge = new AlarmJudge();
        // 设置临界阈值比例为0.9 (90%)
        ReflectionTestUtils.setField(alarmJudge, "criticalRatio", 0.9);
    }

    @Test
    @DisplayName("判定测试 - 合格 (>=100%)")
    void judge_Passed() {
        // 实际12分钟，标准12分钟，达到100%
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(720) // 12分钟
                .standardSeconds(720) // 12分钟
                .deviationSeconds(0)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);

        assertEquals(ComplianceLevel.PASSED, level);
    }

    @Test
    @DisplayName("判定测试 - 临界 (>=90%, <100%)")
    void judge_Critical() {
        // 实际11分钟，标准12分钟，约91.7%
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(660) // 11分钟
                .standardSeconds(720) // 12分钟
                .deviationSeconds(-60)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);

        assertEquals(ComplianceLevel.CRITICAL, level);
    }

    @Test
    @DisplayName("判定测试 - 不合格 (<90%)")
    void judge_Failed() {
        // 实际10分钟，标准12分钟，约83.3%
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(600) // 10分钟
                .standardSeconds(720) // 12分钟
                .deviationSeconds(-120)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);

        assertEquals(ComplianceLevel.FAILED, level);
    }

    @Test
    @DisplayName("边界测试 - 刚好90%应为临界")
    void judge_Boundary_Critical() {
        // 临界阈值: 720 * 0.9 = 648秒 (10.8分钟)
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(648) // 正好90%
                .standardSeconds(720)
                .deviationSeconds(-72)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);

        assertEquals(ComplianceLevel.CRITICAL, level);
    }

    @Test
    @DisplayName("边界测试 - 略低于90%应为不合格")
    void judge_Boundary_Failed() {
        // 略低于90%
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(647) // 略低于90%
                .standardSeconds(720)
                .deviationSeconds(-73)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);

        assertEquals(ComplianceLevel.FAILED, level);
    }

    @Test
    @DisplayName("判定详情测试 - 合格")
    void judgeWithDetails_Passed() {
        TaskDurationResult durationResult = TaskDurationResult.builder()
                .channelId(1L)
                .actualSeconds(720)
                .standardSeconds(720)
                .deviationSeconds(0)
                .build();

        AlarmJudgeResult result = alarmJudge.judgeWithDetails(durationResult, 1L, "左前门");

        assertEquals(ComplianceLevel.PASSED, result.getComplianceLevel());
        assertFalse(result.isNeedAlarm());
        assertEquals(AlarmTypeEnum.PDI_VIOLATION, result.getAlarmType());
        assertNotNull(result.getDescription());
        assertTrue(result.getDescription().contains("达标"));
    }

    @Test
    @DisplayName("判定详情测试 - 临界")
    void judgeWithDetails_Critical() {
        TaskDurationResult durationResult = TaskDurationResult.builder()
                .channelId(1L)
                .actualSeconds(660)
                .standardSeconds(720)
                .deviationSeconds(-60)
                .build();

        AlarmJudgeResult result = alarmJudge.judgeWithDetails(durationResult, 1L, "左前门");

        assertEquals(ComplianceLevel.CRITICAL, result.getComplianceLevel());
        assertFalse(result.isNeedAlarm()); // 临界不需要报警
        assertTrue(result.getDescription().contains("临界"));
    }

    @Test
    @DisplayName("判定详情测试 - 不合格/报警")
    void judgeWithDetails_Failed() {
        TaskDurationResult durationResult = TaskDurationResult.builder()
                .channelId(1L)
                .actualSeconds(600)
                .standardSeconds(720)
                .deviationSeconds(-120)
                .build();

        AlarmJudgeResult result = alarmJudge.judgeWithDetails(durationResult, 1L, "左前门");

        assertEquals(ComplianceLevel.FAILED, result.getComplianceLevel());
        assertTrue(result.isNeedAlarm()); // 不合格需要报警
        assertEquals(AlarmTypeEnum.PDI_VIOLATION, result.getAlarmType());
        assertTrue(result.getDescription().contains("未达标"));
    }

    @Test
    @DisplayName("判定详情测试 - 空位置信息")
    void judgeWithDetails_NullLocation() {
        TaskDurationResult durationResult = TaskDurationResult.builder()
                .actualSeconds(600)
                .standardSeconds(720)
                .deviationSeconds(-120)
                .build();

        AlarmJudgeResult result = alarmJudge.judgeWithDetails(durationResult, 1L, null);

        assertTrue(result.getDescription().contains("未知位置"));
    }

    @Test
    @DisplayName("needAlarm测试 - 合格不需要报警")
    void needAlarm_Passed_ReturnsFalse() {
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(720)
                .standardSeconds(720)
                .build();

        assertFalse(alarmJudge.needAlarm(result));
    }

    @Test
    @DisplayName("needAlarm测试 - 临界不需要报警")
    void needAlarm_Critical_ReturnsFalse() {
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(660)
                .standardSeconds(720)
                .build();

        assertFalse(alarmJudge.needAlarm(result));
    }

    @Test
    @DisplayName("needAlarm测试 - 不合格需要报警")
    void needAlarm_Failed_ReturnsTrue() {
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(600)
                .standardSeconds(720)
                .build();

        assertTrue(alarmJudge.needAlarm(result));
    }

    @Test
    @DisplayName("不同临界阈值测试 - 0.8阈值")
    void judge_CustomCriticalRatio_08() {
        // 设置临界阈值为80%
        ReflectionTestUtils.setField(alarmJudge, "criticalRatio", 0.8);

        // 85%的完成度 (720*0.85=612秒)
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(612)
                .standardSeconds(720)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);
        assertEquals(ComplianceLevel.CRITICAL, level);
    }

    @Test
    @DisplayName("不同临界阈值测试 - 0.95阈值")
    void judge_CustomCriticalRatio_095() {
        // 设置临界阈值为95%
        ReflectionTestUtils.setField(alarmJudge, "criticalRatio", 0.95);

        // 92%的完成度 (720*0.92=662秒)
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(662)
                .standardSeconds(720)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);
        assertEquals(ComplianceLevel.FAILED, level); // 92% < 95%，应为不合格
    }

    @Test
    @DisplayName("描述格式测试 - 包含时间信息")
    void descriptionFormat_ContainsDurationInfo() {
        TaskDurationResult durationResult = TaskDurationResult.builder()
                .channelId(1L)
                .actualSeconds(600) // 10分钟
                .standardSeconds(720) // 12分钟
                .deviationSeconds(-120)
                .build();

        AlarmJudgeResult result = alarmJudge.judgeWithDetails(durationResult, 1L, "左前门");

        String description = result.getDescription();
        assertTrue(description.contains("10分0秒"));
        assertTrue(description.contains("12分0秒"));
        assertTrue(description.contains("2分0秒"));
    }

    @Test
    @DisplayName("长时长测试 - 超过一小时")
    void judge_LongDuration() {
        TaskDurationResult result = TaskDurationResult.builder()
                .actualSeconds(5400) // 90分钟
                .standardSeconds(3600) // 60分钟标准
                .deviationSeconds(1800)
                .build();

        ComplianceLevel level = alarmJudge.judge(result);

        assertEquals(ComplianceLevel.PASSED, level);
    }
}
