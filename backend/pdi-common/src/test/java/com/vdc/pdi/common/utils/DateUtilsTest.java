
package com.vdc.pdi.common.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateUtils 单元测试
 */
class DateUtilsTest {

    @Test
    void testNow() {
        LocalDate now = DateUtils.now();
        assertNotNull(now);
        assertEquals(LocalDate.now(), now);
    }

    @Test
    void testNowDateTime() {
        LocalDateTime now = DateUtils.nowDateTime();
        assertNotNull(now);
        // 允许1秒内的误差
        assertTrue(Math.abs(now.getSecond() - LocalDateTime.now().getSecond()) <= 1);
    }

    @Test
    void testCurrentTimeMillis() {
        long time = DateUtils.currentTimeMillis();
        assertTrue(time > 0);
        assertTrue(Math.abs(time - System.currentTimeMillis()) < 1000);
    }

    @Test
    void testFormat() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        assertEquals("2024-03-15", DateUtils.format(date));

        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30, 0);
        assertEquals("2024-03-15 10:30:00", DateUtils.format(dateTime));

        assertNull(DateUtils.format((LocalDate) null));
        assertNull(DateUtils.format((LocalDateTime) null));
    }

    @Test
    void testFormatWithMillis() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30, 0, 123000000);
        assertEquals("2024-03-15 10:30:00.123", DateUtils.formatWithMillis(dateTime));
    }

    @Test
    void testParseDate() {
        LocalDate date = DateUtils.parseDate("2024-03-15");
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(3, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());

        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
    }

    @Test
    void testParseDateTime() {
        LocalDateTime dateTime = DateUtils.parseDateTime("2024-03-15 10:30:00");
        assertNotNull(dateTime);
        assertEquals(2024, dateTime.getYear());
        assertEquals(10, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());

        assertNull(DateUtils.parseDateTime(null));
    }

    @Test
    void testToDateAndToLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30, 0);
        Date date = DateUtils.toDate(dateTime);
        assertNotNull(date);

        LocalDateTime converted = DateUtils.toLocalDateTime(date);
        assertEquals(dateTime.getYear(), converted.getYear());
        assertEquals(dateTime.getHour(), converted.getHour());

        assertNull(DateUtils.toDate((LocalDateTime) null));
        assertNull(DateUtils.toLocalDateTime(null));
    }

    @Test
    void testToDateAndToLocalDate() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        Date utilDate = DateUtils.toDate(date);
        assertNotNull(utilDate);

        LocalDate converted = DateUtils.toLocalDate(utilDate);
        assertEquals(date, converted);

        assertNull(DateUtils.toDate((LocalDate) null));
        assertNull(DateUtils.toLocalDate(null));
    }

    @Test
    void testPlusDays() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime result = DateUtils.plusDays(dateTime, 5);
        assertEquals(20, result.getDayOfMonth());

        assertNull(DateUtils.plusDays(null, 5));
    }

    @Test
    void testMinusDays() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime result = DateUtils.minusDays(dateTime, 5);
        assertEquals(10, result.getDayOfMonth());
    }

    @Test
    void testDaysBetween() {
        LocalDate start = LocalDate.of(2024, 3, 10);
        LocalDate end = LocalDate.of(2024, 3, 15);
        assertEquals(5, DateUtils.daysBetween(start, end));

        assertEquals(0, DateUtils.daysBetween(null, end));
    }

    @Test
    void testStartOfDay() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalDateTime start = DateUtils.startOfDay(date);
        assertEquals(LocalTime.MIN, start.toLocalTime());

        assertNull(DateUtils.startOfDay(null));
    }

    @Test
    void testEndOfDay() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalDateTime end = DateUtils.endOfDay(date);
        assertEquals(LocalTime.MAX, end.toLocalTime());
    }

    @Test
    void testIsToday() {
        assertTrue(DateUtils.isToday(LocalDate.now()));
        assertFalse(DateUtils.isToday(LocalDate.now().minusDays(1)));
        assertFalse(DateUtils.isToday(null));
    }

    @Test
    void testIsBetween() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalDate start = LocalDate.of(2024, 3, 10);
        LocalDate end = LocalDate.of(2024, 3, 20);

        assertTrue(DateUtils.isBetween(date, start, end));
        assertTrue(DateUtils.isBetween(start, start, end));
        assertFalse(DateUtils.isBetween(LocalDate.of(2024, 3, 25), start, end));
        assertFalse(DateUtils.isBetween(null, start, end));
    }
}
