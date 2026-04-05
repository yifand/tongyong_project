package com.vdc.pdi.common.enums;

/**
 * 告警状态枚举
 */
public enum AlarmStatusEnum implements EnumCode<Integer> {

    UNPROCESSED(0, "未处理", "报警待处理"),
    PROCESSED(1, "已处理", "报警已确认处理"),
    FALSE_POSITIVE(2, "误报", "报警被标记为误报");

    private final Integer code;
    private final String name;
    private final String description;

    AlarmStatusEnum(Integer code, String name, String description) {
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
    public static AlarmStatusEnum fromCode(Integer code) {
        for (AlarmStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid alarm status code: " + code);
    }

    /**
     * 是否已处理（包含已处理和误报）
     */
    public boolean isProcessed() {
        return this == PROCESSED || this == FALSE_POSITIVE;
    }
}
