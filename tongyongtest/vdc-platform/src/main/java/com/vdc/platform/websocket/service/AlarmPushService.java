package com.vdc.platform.websocket.service;

import com.vdc.platform.entity.Alarm;
import com.vdc.platform.service.IAlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmPushService implements MessageListener {

    private final IAlarmService alarmService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String alarmIdStr = new String(message.getBody(), StandardCharsets.UTF_8);
        log.debug("Received Redis alarm message: {}", alarmIdStr);
        try {
            Long alarmId = Long.valueOf(alarmIdStr);
            Alarm alarm = alarmService.getById(alarmId);
            if (alarm != null) {
                messagingTemplate.convertAndSend("/topic/alarms", alarm);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid alarm id from Redis: {}", alarmIdStr);
        }
    }
}
