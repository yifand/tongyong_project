package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通道类型枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum ChannelTypeEnum {

    PDI_DETECTION(1, "PDI检测"),
    SMOKING_DETECTION(2, "吸烟检测");

    private final Integer code;
    private final String name;

    public static ChannelTypeEnum getByCode(Integer code) {
        for (ChannelTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
