package com.vdc.pdi.common.enums;

/**
 * 告警类型枚举
 */
public enum AlarmTypeEnum implements EnumCode<Integer> {

    SMOKE(0, "抽烟", "抽烟行为检测"),
    PDI_VIOLATION(1, "PDI违规", "PDI作业时长不达标");

    private final Integer code;
    private final String name;
    private final String description;

    AlarmTypeEnum(Integer code, String name, String description) {
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
    public static AlarmTypeEnum fromCode(Integer code) {
        for (AlarmTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid alarm type code: " + code);
    }
}
