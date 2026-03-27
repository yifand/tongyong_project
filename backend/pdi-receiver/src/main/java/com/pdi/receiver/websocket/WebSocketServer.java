package com.pdi.receiver.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * WebSocket服务端（预留扩展用）
 * 
 * 当前使用Spring的WebSocketHandler处理WebSocket连接
 * 此类预留用于后续扩展，如：
 * - 自定义WebSocket服务器
 * - 多协议支持（STOMP等）
 * - 集群WebSocket支持
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class WebSocketServer {

    @PostConstruct
    public void init() {
        log.info("WebSocket服务初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("WebSocket服务已关闭");
    }

}
