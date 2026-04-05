package com.vdc.pdi.ruleengine.domain.vo;

import com.vdc.pdi.common.enums.AlarmTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报警判定结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmJudgeResult {

    /**
     * 合规等级
     */
    private ComplianceLevel complianceLevel;

    /**
     * 是否需要报警
     */
    private boolean needAlarm;

    /**
     * 报警类型
     */
    private AlarmTypeEnum alarmType;

    /**
     * 报警描述
     */
    private String description;

    /**
     * 作业时长结果
     */
    private TaskDurationResult durationResult;
}
