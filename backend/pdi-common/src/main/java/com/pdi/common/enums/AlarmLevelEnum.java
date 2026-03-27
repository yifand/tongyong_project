package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报警级别枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum AlarmLevelEnum {

    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高"),
    EMERGENCY(4, "紧急");

    private final Integer code;
    private final String name;

    public static AlarmLevelEnum getByCode(Integer code) {
        for (AlarmLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }

}
