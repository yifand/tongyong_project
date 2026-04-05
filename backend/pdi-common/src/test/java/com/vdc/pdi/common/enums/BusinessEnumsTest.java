package com.vdc.pdi.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务枚举类单元测试
 */
class BusinessEnumsTest {

    @Test
    void testSiteEnum() {
        assertEquals("金桥", SiteEnum.JINQIAO.getMessage());
        assertEquals(1, SiteEnum.JINQIAO.getCode());
        assertEquals("JQ", SiteEnum.JINQIAO.getShortCode());
        assertEquals(SiteEnum.JINQIAO, SiteEnum.fromCode(1));
        assertEquals(SiteEnum.KAIDI, SiteEnum.fromShortCode("KD"));
        assertThrows(IllegalArgumentException.class, () -> SiteEnum.fromCode(999));
    }

    @Test
    void testAlarmTypeEnum() {
        assertEquals("抽烟", AlarmTypeEnum.SMOKE.getMessage());
        assertEquals(0, AlarmTypeEnum.SMOKE.getCode());
        assertEquals("抽烟行为检测", AlarmTypeEnum.SMOKE.getDescription());
        assertEquals(AlarmTypeEnum.PDI_VIOLATION, AlarmTypeEnum.fromCode(1));
        assertThrows(IllegalArgumentException.class, () -> AlarmTypeEnum.fromCode(999));
    }

    @Test
    void testAlarmStatusEnum() {
        assertEquals(0, AlarmStatusEnum.UNPROCESSED.getCode());
        assertEquals(1, AlarmStatusEnum.PROCESSED.getCode());
        assertEquals(2, AlarmStatusEnum.FALSE_POSITIVE.getCode());

        assertFalse(AlarmStatusEnum.UNPROCESSED.isProcessed());
        assertTrue(AlarmStatusEnum.PROCESSED.isProcessed());
        assertTrue(AlarmStatusEnum.FALSE_POSITIVE.isProcessed());

        assertThrows(IllegalArgumentException.class, () -> AlarmStatusEnum.fromCode(999));
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
        assertEquals(1, StateCodeEnum.S1.getCode());
        assertEquals(3, StateCodeEnum.S3.getCode());
        assertEquals("空闲", StateCodeEnum.S1.getMessage());

        assertEquals(StateCodeEnum.S1, StateCodeEnum.fromState(0, 0, 0));
        assertEquals(StateCodeEnum.S3, StateCodeEnum.fromState(0, 1, 0));
        assertEquals(StateCodeEnum.S5, StateCodeEnum.fromState(1, 0, 0));
        assertEquals(StateCodeEnum.S7, StateCodeEnum.fromState(1, 1, 0));
        assertEquals(StateCodeEnum.S8, StateCodeEnum.fromState(1, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> StateCodeEnum.fromState(0, 0, 1));
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
        assertEquals(0, ChannelTypeEnum.VIDEO_STREAM.getCode());
        assertEquals(1, ChannelTypeEnum.SNAPSHOT.getCode());
        assertEquals("实时视频流", ChannelTypeEnum.VIDEO_STREAM.getDescription());
        assertEquals("抓拍图片", ChannelTypeEnum.SNAPSHOT.getDescription());
        assertEquals(ChannelTypeEnum.SNAPSHOT, ChannelTypeEnum.fromCode(1));
        assertThrows(IllegalArgumentException.class, () -> ChannelTypeEnum.fromCode(999));
    }
}
