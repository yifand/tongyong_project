
package com.vdc.pdi.common.enums;

/**
 * 告警类型枚举
 */
public enum AlarmTypeEnum implements EnumCode<Integer> {

    /**
     * 越限告警
     */
    THRESHOLD(1, "越限告警"),

    /**
     * 变位告警
     */
    STATUS_CHANGE(2, "变位告警"),

    /**
     * 通信告警
     */
    COMMUNICATION(3, "通信告警"),

    /**
     * 设备告警
     */
    DEVICE(4, "设备告警"),

    /**
     * 安全告警
     */
    SECURITY(5, "安全告警");

    private final Integer code;
    private final String message;

    AlarmTypeEnum(Integer code, String message) {
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
    public static AlarmTypeEnum fromCode(Integer code) {
        for (AlarmTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
