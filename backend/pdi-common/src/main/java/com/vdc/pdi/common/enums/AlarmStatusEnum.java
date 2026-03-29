
package com.vdc.pdi.common.enums;

/**
 * 告警状态枚举
 */
public enum AlarmStatusEnum implements EnumCode<Integer> {

    /**
     * 未确认
     */
    UNCONFIRMED(0, "未确认"),

    /**
     * 已确认
     */
    CONFIRMED(1, "已确认"),

    /**
     * 已清除
     */
    CLEARED(2, "已清除");

    private final Integer code;
    private final String message;

    AlarmStatusEnum(Integer code, String message) {
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
    public static AlarmStatusEnum fromCode(Integer code) {
        for (AlarmStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否已确认
     */
    public boolean isConfirmed() {
        return this == CONFIRMED || this == CLEARED;
    }

    /**
     * 是否已清除
     */
    public boolean isCleared() {
        return this == CLEARED;
    }
}
