package com.vdc.pdi.logmanagement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 */
@Getter
@AllArgsConstructor
public enum OperationType {

    LOGIN(1, "登录", "用户登录系统"),
    LOGOUT(2, "登出", "用户退出系统"),
    CONFIG_MODIFY(3, "配置修改", "修改系统配置"),
    USER_MANAGE(4, "用户管理", "创建/修改/删除用户"),
    ROLE_MANAGE(5, "角色管理", "创建/修改/删除角色"),
    DATA_EXPORT(6, "数据导出", "导出报表或数据"),
    ALARM_PROCESS(7, "报警处理", "处理报警记录"),
    DEVICE_MANAGE(8, "设备管理", "管理边缘盒子或通道"),
    THRESHOLD_CONFIG(9, "阈值配置", "修改算法阈值配置"),
    OTHER(99, "其他", "其他操作");

    private final Integer code;
    private final String name;
    private final String description;

    public static OperationType fromCode(Integer code) {
        if (code == null) {
            return OTHER;
        }
        for (OperationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
