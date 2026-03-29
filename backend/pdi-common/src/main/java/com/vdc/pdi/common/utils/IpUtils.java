
package com.vdc.pdi.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP地址工具类
 * 提供获取客户端IP地址等功能
 */
public final class IpUtils {

    private static final Logger logger = LoggerFactory.getLogger(IpUtils.class);

    /**
     * 未知IP
     */
    public static final String UNKNOWN = "unknown";

    /**
     * 本地IP
     */
    public static final String LOCAL_IP = "127.0.0.1";

    /**
     * 本地IPv6
     */
    public static final String LOCAL_IP_IPV6 = "0:0:0:0:0:0:0:1";

    private IpUtils() {
        // 私有构造，防止实例化
    }

    /**
     * 获取客户端真实IP地址
     * 按优先级从多个请求头中获取
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isEmptyOrUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多IP情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 处理IPv6本地地址
        if (LOCAL_IP_IPV6.equals(ip)) {
            ip = LOCAL_IP;
        }

        return ip;
    }

    /**
     * 获取服务器本地IP地址
     *
     * @return 本地IP地址
     */
    public static String getLocalIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("获取本地IP地址失败", e);
            return LOCAL_IP;
        }
    }

    /**
     * 获取服务器主机名
     *
     * @return 主机名
     */
    public static String getHostName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            logger.error("获取主机名失败", e);
            return UNKNOWN;
        }
    }

    /**
     * 判断IP是否为空或未知
     */
    private static boolean isEmptyOrUnknown(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 检查是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // 本地地址
        if (LOCAL_IP.equals(ip) || LOCAL_IP_IPV6.equals(ip)) {
            return true;
        }

        // 检查内网地址段
        // 10.x.x.x
        if (ip.startsWith("10.")) {
            return true;
        }
        // 172.16.x.x - 172.31.x.x
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        // 192.168.x.x
        if (ip.startsWith("192.168.")) {
            return true;
        }
        // 127.x.x.x
        if (ip.startsWith("127.")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为有效的IPv4地址
     *
     * @param ip IP地址
     * @return 是否有效
     */
    public static boolean isValidIpv4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}
