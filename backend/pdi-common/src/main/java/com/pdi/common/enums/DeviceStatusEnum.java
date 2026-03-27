package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备状态枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum DeviceStatusEnum {

    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    FAULT(2, "故障");

    private final Integer code;
    private final String name;

    public static DeviceStatusEnum getByCode(Integer code) {
        for (DeviceStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
