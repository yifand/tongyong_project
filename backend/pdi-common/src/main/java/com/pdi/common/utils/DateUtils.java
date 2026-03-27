package com.pdi.common.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * 日期时间工具类
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public class DateUtils {

    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    private DateUtils() {
    }

    /**
     * 获取当前日期时间字符串
     */
    public static String now() {
        return DateUtil.format(LocalDateTime.now(), DEFAULT_DATE_TIME_PATTERN);
    }

    /**
     * 格式化日期时间
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DateUtil.format(dateTime, DEFAULT_DATE_TIME_PATTERN);
    }

    /**
     * 格式化日期时间 - 指定格式
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return DateUtil.format(dateTime, pattern);
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
    }

    /**
     * 获取当天开始时间
     */
    public static LocalDateTime getDayStart() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    /**
     * 获取当天结束时间
     */
    public static LocalDateTime getDayEnd() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
    }

    /**
     * 获取指定日期的开始时间
     */
    public static LocalDateTime getDayStart(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MIN);
    }

    /**
     * 获取指定日期的结束时间
     */
    public static LocalDateTime getDayEnd(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MAX);
    }

    /**
     * 获取本周开始时间
     */
    public static LocalDateTime getWeekStart() {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)), LocalTime.MIN);
    }

    /**
     * 获取本周结束时间
     */
    public static LocalDateTime getWeekEnd() {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)), LocalTime.MAX);
    }

    /**
     * 获取本月开始时间
     */
    public static LocalDateTime getMonthStart() {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
    }

    /**
     * 获取本月结束时间
     */
    public static LocalDateTime getMonthEnd() {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);
    }

}
