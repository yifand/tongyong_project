package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据范围枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    ALL(1, "全部数据"),
    CURRENT_SITE(2, "本站点数据"),
    CURRENT_USER(3, "本人数据");

    private final Integer code;
    private final String description;

    public static DataScopeEnum getByCode(Integer code) {
        for (DataScopeEnum scope : values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        return null;
    }

}
