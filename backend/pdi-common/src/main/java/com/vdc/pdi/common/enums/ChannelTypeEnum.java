
package com.vdc.pdi.common.enums;

/**
 * 通道类型枚举
 * 用于设备通信通道
 */
public enum ChannelTypeEnum implements EnumCode<Integer> {

    /**
     * 串口
     */
    SERIAL(1, "串口"),

    /**
     * 以太网
     */
    ETHERNET(2, "以太网"),

    /**
     * 无线4G
     */
    WIRELESS_4G(3, "无线4G"),

    /**
     * 无线5G
     */
    WIRELESS_5G(4, "无线5G"),

    /**
     * LoRa
     */
    LORA(5, "LoRa"),

    /**
     * NB-IoT
     */
    NB_IOT(6, "NB-IoT"),

    /**
     * 光纤
     */
    FIBER(7, "光纤");

    private final Integer code;
    private final String message;

    ChannelTypeEnum(Integer code, String message) {
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
    public static ChannelTypeEnum fromCode(Integer code) {
        for (ChannelTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否为无线通道
     */
    public boolean isWireless() {
        return this == WIRELESS_4G || this == WIRELESS_5G || this == LORA || this == NB_IOT;
    }

    /**
     * 是否为有线通道
     */
    public boolean isWired() {
        return this == SERIAL || this == ETHERNET || this == FIBER;
    }
}
