package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限类型枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum PermissionTypeEnum {

    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    API(3, "接口");

    private final Integer code;
    private final String description;

    public static PermissionTypeEnum getByCode(Integer code) {
        for (PermissionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
