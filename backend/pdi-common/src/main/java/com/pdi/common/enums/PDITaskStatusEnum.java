package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PDI作业状态枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum PDITaskStatusEnum {

    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    INTERRUPTED(3, "异常中断");

    private final Integer code;
    private final String name;

    public static PDITaskStatusEnum getByCode(Integer code) {
        for (PDITaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
