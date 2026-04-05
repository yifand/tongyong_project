package com.vdc.pdi.behaviorarchive.enums;

/**
 * 档案状态枚举
 * 0-进行中，1-达标，2-未达标
 */
public enum ArchiveStatus {

    IN_PROGRESS(0, "进行中", "warning"),
    QUALIFIED(1, "达标", "success"),
    UNQUALIFIED(2, "未达标", "danger");

    private final Integer code;
    private final String text;
    private final String tagType;

    ArchiveStatus(Integer code, String text, String tagType) {
        this.code = code;
        this.text = text;
        this.tagType = tagType;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public String getTagType() {
        return tagType;
    }

    /**
     * 根据编码获取枚举
     */
    public static ArchiveStatus fromCode(Integer code) {
        if (code == null) {
            return IN_PROGRESS;
        }
        for (ArchiveStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return IN_PROGRESS;
    }

    /**
     * 判断是否达标
     */
    public boolean isQualified() {
        return this == QUALIFIED;
    }

    /**
     * 判断是否未达标
     */
    public boolean isUnqualified() {
        return this == UNQUALIFIED;
    }

    /**
     * 判断是否在进行中
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }
}
