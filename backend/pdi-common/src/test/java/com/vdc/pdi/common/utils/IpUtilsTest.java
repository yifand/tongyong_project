
package com.vdc.pdi.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IpUtils 单元测试
 */
class IpUtilsTest {

    @Test
    void testGetLocalIp() {
        String ip = IpUtils.getLocalIp();
        assertNotNull(ip);
        assertNotEquals(IpUtils.UNKNOWN, ip);
    }

    @Test
    void testGetHostName() {
        String hostName = IpUtils.getHostName();
        assertNotNull(hostName);
        assertNotEquals(IpUtils.UNKNOWN, hostName);
    }

    @Test
    void testIsValidIpv4() {
        assertTrue(IpUtils.isValidIpv4("192.168.1.1"));
        assertTrue(IpUtils.isValidIpv4("10.0.0.1"));
        assertTrue(IpUtils.isValidIpv4("255.255.255.255"));
        assertTrue(IpUtils.isValidIpv4("0.0.0.0"));

        assertFalse(IpUtils.isValidIpv4("256.1.1.1"));
        assertFalse(IpUtils.isValidIpv4("192.168.1"));
        assertFalse(IpUtils.isValidIpv4("192.168.1.1.1"));
        assertFalse(IpUtils.isValidIpv4("not.an.ip.address"));
        assertFalse(IpUtils.isValidIpv4(null));
        assertFalse(IpUtils.isValidIpv4(""));
    }

    @Test
    void testIsInternalIp() {
        // 内网地址
        assertTrue(IpUtils.isInternalIp("127.0.0.1"));
        assertTrue(IpUtils.isInternalIp("10.0.0.1"));
        assertTrue(IpUtils.isInternalIp("10.255.255.255"));
        assertTrue(IpUtils.isInternalIp("172.16.0.1"));
        assertTrue(IpUtils.isInternalIp("172.31.255.255"));
        assertTrue(IpUtils.isInternalIp("192.168.0.1"));
        assertTrue(IpUtils.isInternalIp("192.168.255.255"));
        assertTrue(IpUtils.isInternalIp("0:0:0:0:0:0:0:1"));

        // 公网地址
        assertFalse(IpUtils.isInternalIp("8.8.8.8"));
        assertFalse(IpUtils.isInternalIp("172.32.0.1"));
        assertFalse(IpUtils.isInternalIp("192.169.0.1"));
        assertFalse(IpUtils.isInternalIp(null));
        assertFalse(IpUtils.isInternalIp(""));
    }

    @Test
    void testConstants() {
        assertEquals("unknown", IpUtils.UNKNOWN);
        assertEquals("127.0.0.1", IpUtils.LOCAL_IP);
        assertEquals("0:0:0:0:0:0:0:1", IpUtils.LOCAL_IP_IPV6);
    }
}
