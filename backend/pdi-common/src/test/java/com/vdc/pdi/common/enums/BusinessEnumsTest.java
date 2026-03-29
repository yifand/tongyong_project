
package com.vdc.pdi.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务枚举类单元测试
 */
class BusinessEnumsTest {

    @Test
    void testSiteEnum() {
        assertEquals("变电站", SiteEnum.SUBSTATION.getMessage());
        assertEquals(1, SiteEnum.SUBSTATION.getCode());
        assertEquals(SiteEnum.SUBSTATION, SiteEnum.fromCode(1));
        assertNull(SiteEnum.fromCode(999));
    }

    @Test
    void testAlarmTypeEnum() {
        assertEquals("越限告警", AlarmTypeEnum.THRESHOLD.getMessage());
        assertEquals(1, AlarmTypeEnum.THRESHOLD.getCode());
        assertEquals(AlarmTypeEnum.COMMUNICATION, AlarmTypeEnum.fromCode(3));
        assertNull(AlarmTypeEnum.fromCode(999));
    }

    @Test
    void testAlarmStatusEnum() {
        assertEquals(0, AlarmStatusEnum.UNCONFIRMED.getCode());
        assertEquals(1, AlarmStatusEnum.CONFIRMED.getCode());
        assertEquals(2, AlarmStatusEnum.CLEARED.getCode());

        assertFalse(AlarmStatusEnum.UNCONFIRMED.isConfirmed());
        assertTrue(AlarmStatusEnum.CONFIRMED.isConfirmed());
        assertTrue(AlarmStatusEnum.CLEARED.isConfirmed());

        assertFalse(AlarmStatusEnum.UNCONFIRMED.isCleared());
        assertFalse(AlarmStatusEnum.CONFIRMED.isCleared());
        assertTrue(AlarmStatusEnum.CLEARED.isCleared());
    }

    @Test
    void testArchiveStatusEnum() {
        assertEquals(0, ArchiveStatusEnum.UNARCHIVED.getCode());
        assertEquals(1, ArchiveStatusEnum.ARCHIVED.getCode());
        assertEquals(2, ArchiveStatusEnum.FAILED.getCode());

        assertFalse(ArchiveStatusEnum.UNARCHIVED.isArchived());
        assertTrue(ArchiveStatusEnum.ARCHIVED.isArchived());
        assertFalse(ArchiveStatusEnum.FAILED.isArchived());
    }

    @Test
    void testStateCodeEnum() {
        assertEquals(0, StateCodeEnum.NORMAL.getCode());
        assertEquals(1, StateCodeEnum.COMM_ERROR.getCode());

        assertTrue(StateCodeEnum.NORMAL.isValid());
        assertTrue(StateCodeEnum.MANUAL_SET.isValid());
        assertFalse(StateCodeEnum.COMM_ERROR.isValid());

        assertTrue(StateCodeEnum.COMM_ERROR.isError());
        assertTrue(StateCodeEnum.INVALID_DATA.isError());
        assertFalse(StateCodeEnum.NORMAL.isError());
    }

    @Test
    void testDeviceStatusEnum() {
        assertEquals(0, DeviceStatusEnum.OFFLINE.getCode());
        assertEquals(1, DeviceStatusEnum.ONLINE.getCode());

        assertTrue(DeviceStatusEnum.ONLINE.isOnline());
        assertFalse(DeviceStatusEnum.OFFLINE.isOnline());

        assertTrue(DeviceStatusEnum.ONLINE.isAvailable());
        assertTrue(DeviceStatusEnum.MAINTENANCE.isAvailable());
        assertFalse(DeviceStatusEnum.OFFLINE.isAvailable());
    }

    @Test
    void testChannelTypeEnum() {
        assertEquals(1, ChannelTypeEnum.SERIAL.getCode());
        assertEquals(2, ChannelTypeEnum.ETHERNET.getCode());
        assertEquals(3, ChannelTypeEnum.WIRELESS_4G.getCode());

        assertTrue(ChannelTypeEnum.WIRELESS_4G.isWireless());
        assertTrue(ChannelTypeEnum.LORA.isWireless());
        assertTrue(ChannelTypeEnum.ETHERNET.isWired());
        assertFalse(ChannelTypeEnum.ETHERNET.isWireless());
    }
}
