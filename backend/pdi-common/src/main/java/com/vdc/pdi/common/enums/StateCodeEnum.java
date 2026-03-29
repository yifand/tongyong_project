
package com.vdc.pdi.common.enums;

/**
 * 状态码类型枚举
 * 用于实时数据、历史数据等状态标识
 */
public enum StateCodeEnum implements EnumCode<Integer> {

    /**
     * 正常
     */
    NORMAL(0, "正常"),

    /**
     * 通信故障
     */
    COMM_ERROR(1, "通信故障"),

    /**
     * 数据无效
     */
    INVALID_DATA(2, "数据无效"),

    /**
     * 人工置数
     */
    MANUAL_SET(3, "人工置数"),

    /**
     * 数据溢出
     */
    OVERFLOW(4, "数据溢出"),

    /**
     * 工程单位溢出
     */
    ENG_OVERFLOW(5, "工程单位溢出"),

    /**
     * 原始数据溢出
     */
    RAW_OVERFLOW(6, "原始数据溢出"),

    /**
     * 遥信变位
     */
    STATUS_CHANGE(7, "遥信变位"),

    /**
     * 检修状态
     */
    MAINTENANCE(8, "检修状态"),

    /**
     * 备用
     */
    RESERVED(9, "备用");

    private final Integer code;
    private final String message;

    StateCodeEnum(Integer code, String message) {
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
    public static StateCodeEnum fromCode(Integer code) {
        for (StateCodeEnum state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        return null;
    }

    /**
     * 是否为有效数据
     */
    public boolean isValid() {
        return this == NORMAL || this == MANUAL_SET || this == STATUS_CHANGE;
    }

    /**
     * 是否为故障状态
     */
    public boolean isError() {
        return this == COMM_ERROR || this == INVALID_DATA || this == OVERFLOW
                || this == ENG_OVERFLOW || this == RAW_OVERFLOW;
    }
}
