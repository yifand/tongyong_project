
package com.vdc.pdi.common.enums;

/**
 * 站点类型枚举
 */
public enum SiteEnum implements EnumCode<Integer> {

    /**
     * 变电站
     */
    SUBSTATION(1, "变电站"),

    /**
     * 开闭所
     */
    SWITCH_STATION(2, "开闭所"),

    /**
     * 配电室
     */
    DISTRIBUTION_ROOM(3, "配电室"),

    /**
     * 环网柜
     */
    RING_MAIN_UNIT(4, "环网柜"),

    /**
     * 箱变
     */
    BOX_SUBSTATION(5, "箱变"),

    /**
     * 柱上变压器
     */
    POLE_MOUNTED_TRANSFORMER(6, "柱上变压器");

    private final Integer code;
    private final String message;

    SiteEnum(Integer code, String message) {
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
    public static SiteEnum fromCode(Integer code) {
        for (SiteEnum site : values()) {
            if (site.code.equals(code)) {
                return site;
            }
        }
        return null;
    }
}
