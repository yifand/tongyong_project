package com.vdc.platform.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class AlarmWebSocketController {

    @MessageMapping("/alarms")
    @SendTo("/topic/alarms")
    public String handle(String message) {
        return message;
    }
}
