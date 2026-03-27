package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PDI作业结果枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum PDITaskResultEnum {

    QUALIFIED(1, "合格"),
    TIMEOUT(2, "超时"),
    ABNORMAL(3, "异常");

    private final Integer code;
    private final String name;

    public static PDITaskResultEnum getByCode(Integer code) {
        for (PDITaskResultEnum result : values()) {
            if (result.getCode().equals(code)) {
                return result;
            }
        }
        return null;
    }

}
