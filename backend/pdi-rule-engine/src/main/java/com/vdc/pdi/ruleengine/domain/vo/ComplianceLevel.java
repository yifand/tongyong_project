package com.vdc.pdi.ruleengine.domain.vo;

import lombok.Getter;

/**
 * 合规等级枚举
 */
@Getter
public enum ComplianceLevel {

    /**
     * 合格
     */
    PASSED(1, "合格"),

    /**
     * 临界
     */
    CRITICAL(2, "临界"),

    /**
     * 不合格
     */
    FAILED(3, "不合格");

    private final int code;
    private final String description;

    ComplianceLevel(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     */
    public static ComplianceLevel fromCode(int code) {
        for (ComplianceLevel value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid compliance level code: " + code);
    }
}
