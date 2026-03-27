package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 删除标记枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum DeleteEnum {

    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    private final Integer code;
    private final String description;

    public static DeleteEnum getByCode(Integer code) {
        for (DeleteEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
