package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String description;

    public static StatusEnum getByCode(Integer code) {
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
