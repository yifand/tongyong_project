package com.pdi.receiver.websocket;

import com.alibaba.fastjson2.JSON;
import com.pdi.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 报警WebSocket处理器
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Component
public class AlarmWebSocketHandler extends TextWebSocketHandler {

    /**
     * WebSocket会话集合
     */
    private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    /**
     * 用户会话映射
     */
    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket连接建立: sessionId={}, 当前连接数={}", 
                session.getId(), sessions.size());
        
        // 发送连接成功消息
        sendMessage(session, Result.success("连接成功"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        userSessions.values().remove(session);
        log.info("WebSocket连接关闭: sessionId={}, status={}, 当前连接数={}", 
                session.getId(), status, sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: sessionId={}, message={}", session.getId(), payload);
        
        // 处理客户端消息（如心跳、订阅等）
        try {
            Map<String, Object> msg = JSON.parseObject(payload, Map.class);
            String type = (String) msg.get("type");
            
            if ("ping".equals(type)) {
                // 心跳响应
                sendMessage(session, Result.success(Map.of("type", "pong", "timestamp", System.currentTimeMillis())));
            } else if ("subscribe".equals(type)) {
                // 订阅频道
                String channel = (String) msg.get("channel");
                log.info("客户端订阅频道: sessionId={}, channel={}", session.getId(), channel);
                sendMessage(session, Result.success(Map.of("type", "subscribed", "channel", channel)));
            }
        } catch (Exception e) {
            log.warn("处理WebSocket消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
        sessions.remove(session);
        userSessions.values().remove(session);
    }

    /**
     * 发送报警消息给所有客户端
     *
     * @param alarm 报警数据
     */
    public void sendAlarm(Object alarm) {
        if (sessions.isEmpty()) {
            return;
        }

        Result<Object> result = Result.success(alarm);
        result.setCode(2001); // 报警消息类型
        String message = JSON.toJSONString(result);
        
        TextMessage textMessage = new TextMessage(message);
        
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送报警消息失败: sessionId={}", session.getId(), e);
                }
            }
        }
    }

    /**
     * 发送统计更新给所有客户端
     *
     * @param statistics 统计数据
     */
    public void sendStatisticsUpdate(Object statistics) {
        if (sessions.isEmpty()) {
            return;
        }

        Result<Object> result = Result.success(statistics);
        result.setCode(2002); // 统计更新消息类型
        String message = JSON.toJSONString(result);
        
        TextMessage textMessage = new TextMessage(message);
        
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送统计更新失败: sessionId={}", session.getId(), e);
                }
            }
        }
    }

    /**
     * 发送状态更新给所有客户端
     *
     * @param stateUpdate 状态更新数据
     */
    public void sendStateUpdate(Object stateUpdate) {
        if (sessions.isEmpty()) {
            return;
        }

        Result<Object> result = Result.success(stateUpdate);
        result.setCode(2003); // 状态更新消息类型
        String message = JSON.toJSONString(result);
        
        TextMessage textMessage = new TextMessage(message);
        
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送状态更新失败: sessionId={}", session.getId(), e);
                }
            }
        }
    }

    /**
     * 获取当前连接数
     *
     * @return 连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 发送消息给指定会话
     *
     * @param session 会话
     * @param data    数据
     */
    private void sendMessage(WebSocketSession session, Object data) {
        if (session.isOpen()) {
            try {
                String message = JSON.toJSONString(data);
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败: sessionId={}", session.getId(), e);
            }
        }
    }

}
