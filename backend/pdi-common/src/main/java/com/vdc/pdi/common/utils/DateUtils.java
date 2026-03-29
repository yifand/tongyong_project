
package com.vdc.pdi.common.utils;

import com.vdc.pdi.common.constant.CommonConstant;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 */
public final class DateUtils {

    private DateUtils() {
        // 私有构造，防止实例化
    }

    // ========== 日期时间格式化器 ==========

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(CommonConstant.DATE_FORMAT);

    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(CommonConstant.TIME_FORMAT);

    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(CommonConstant.DATETIME_FORMAT);

    public static final DateTimeFormatter DATETIME_MILLIS_FORMATTER =
            DateTimeFormatter.ofPattern(CommonConstant.DATETIME_MILLIS_FORMAT);

    // ========== 当前时间获取 ==========

    /**
     * 获取当前日期
     */
    public static LocalDate now() {
        return LocalDate.now();
    }

    /**
     * 获取当前日期时间
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（秒）
     */
    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / CommonConstant.SECOND_MILLIS;
    }

    // ========== 格式化 ==========

    /**
     * 格式化日期为字符串
     */
    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * 格式化日期时间为字符串
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * 格式化日期时间（带毫秒）
     */
    public static String formatWithMillis(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_MILLIS_FORMATTER) : null;
    }

    /**
     * 按指定格式格式化
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ========== 解析 ==========

    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 按指定格式解析
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty() || pattern == null) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    // ========== 类型转换 ==========

    /**
     * LocalDateTime 转 Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDate 转 Date
     */
    public static Date toDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ========== 时间计算 ==========

    /**
     * 添加天数
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.plusDays(days) : null;
    }

    /**
     * 添加小时
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    /**
     * 添加分钟
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }

    /**
     * 减去天数
     */
    public static LocalDateTime minusDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.minusDays(days) : null;
    }

    /**
     * 获取两个时间的天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 获取一天的开始时间
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * 获取一天的结束时间
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }

    // ========== 判断 ==========

    /**
     * 判断是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * 判断日期是否在指定范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * 判断日期时间是否在指定范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
}
