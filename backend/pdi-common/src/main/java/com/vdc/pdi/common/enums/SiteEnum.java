package com.vdc.pdi.common.enums;

/**
 * 站点枚举
 */
public enum SiteEnum implements EnumCode<Integer> {

    JINQIAO(1, "金桥", "JQ"),
    KAIDI(2, "凯迪", "KD");

    private final Integer code;
    private final String name;
    private final String shortCode;

    SiteEnum(Integer code, String name, String shortCode) {
        this.code = code;
        this.name = name;
        this.shortCode = shortCode;
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

    public String getShortCode() {
        return shortCode;
    }

    /**
     * 根据编码获取枚举
     */
    public static SiteEnum fromCode(Integer code) {
        for (SiteEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid site code: " + code);
    }

    /**
     * 根据短编码获取枚举
     */
    public static SiteEnum fromShortCode(String shortCode) {
        for (SiteEnum value : values()) {
            if (value.getShortCode().equalsIgnoreCase(shortCode)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid site shortCode: " + shortCode);
    }
}
