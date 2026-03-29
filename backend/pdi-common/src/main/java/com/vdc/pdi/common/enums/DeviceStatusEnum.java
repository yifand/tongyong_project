
package com.vdc.pdi.common.enums;

/**
 * 设备状态枚举
 */
public enum DeviceStatusEnum implements EnumCode<Integer> {

    /**
     * 离线
     */
    OFFLINE(0, "离线"),

    /**
     * 在线
     */
    ONLINE(1, "在线"),

    /**
     * 故障
     */
    FAULT(2, "故障"),

    /**
     * 检修
     */
    MAINTENANCE(3, "检修"),

    /**
     * 停用
     */
    DISABLED(4, "停用");

    private final Integer code;
    private final String message;

    DeviceStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 根据编码获取枚举
     */
    public static DeviceStatusEnum fromCode(Integer code) {
        for (DeviceStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否在线
     */
    public boolean isOnline() {
        return this == ONLINE;
    }

    /**
     * 是否可用（在线或检修）
     */
    public boolean isAvailable() {
        return this == ONLINE || this == MAINTENANCE;
    }
}
