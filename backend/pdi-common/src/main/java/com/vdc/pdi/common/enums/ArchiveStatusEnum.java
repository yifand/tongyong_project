
package com.vdc.pdi.common.enums;

/**
 * 归档状态枚举
 */
public enum ArchiveStatusEnum implements EnumCode<Integer> {

    /**
     * 未归档
     */
    UNARCHIVED(0, "未归档"),

    /**
     * 已归档
     */
    ARCHIVED(1, "已归档"),

    /**
     * 归档失败
     */
    FAILED(2, "归档失败");

    private final Integer code;
    private final String message;

    ArchiveStatusEnum(Integer code, String message) {
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
    public static ArchiveStatusEnum fromCode(Integer code) {
        for (ArchiveStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否已归档
     */
    public boolean isArchived() {
        return this == ARCHIVED;
    }
}
