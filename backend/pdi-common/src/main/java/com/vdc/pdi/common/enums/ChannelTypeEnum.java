package com.vdc.pdi.common.enums;

/**
 * 通道类型枚举
 */
public enum ChannelTypeEnum implements EnumCode<Integer> {

    VIDEO_STREAM(0, "视频流", "实时视频流"),
    SNAPSHOT(1, "抓拍机", "抓拍图片");

    private final Integer code;
    private final String name;
    private final String description;

    ChannelTypeEnum(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举
     */
    public static ChannelTypeEnum fromCode(Integer code) {
        for (ChannelTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid channel type code: " + code);
    }
}
