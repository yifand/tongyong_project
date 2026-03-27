package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报警处理状态枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum AlarmHandleStatusEnum {

    UNHANDLED(0, "未处理"),
    CONFIRMED(1, "已确认"),
    HANDLED(2, "已处理"),
    FALSE_POSITIVE(3, "误报");

    private final Integer code;
    private final String name;

    public static AlarmHandleStatusEnum getByCode(Integer code) {
        for (AlarmHandleStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
