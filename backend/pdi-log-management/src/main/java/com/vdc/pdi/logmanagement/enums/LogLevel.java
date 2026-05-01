package com.vdc.pdi.logmanagement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志级别枚举
 */
@Getter
@AllArgsConstructor
public enum LogLevel {

    DEBUG(0, "DEBUG", "调试信息"),
    INFO(1, "INFO", "普通信息"),
    WARN(2, "WARN", "警告信息"),
    ERROR(3, "ERROR", "错误信息");

    private final Integer code;
    private final String name;
    private final String description;

    public static LogLevel fromCode(Integer code) {
        if (code == null) {
            return INFO;
        }
        for (LogLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return INFO;
    }
}
