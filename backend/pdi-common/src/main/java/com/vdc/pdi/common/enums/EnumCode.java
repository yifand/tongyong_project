package com.vdc.pdi.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 枚举统一接口
 * 所有业务枚举需实现此接口，便于统一序列化
 *
 * @param <T> 编码类型
 */
public interface EnumCode<T> {

    /**
     * 获取枚举编码
     */
    T getCode();

    /**
     * 获取枚举描述
     */
    default String getMessage() {
        return "";
    }

    /**
     * 用于JSON序列化
     */
    @JsonValue
    default T getValue() {
        return getCode();
    }
}
