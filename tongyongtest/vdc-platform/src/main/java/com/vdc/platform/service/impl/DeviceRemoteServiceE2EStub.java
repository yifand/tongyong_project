package com.vdc.platform.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Profile("e2e")
public class DeviceRemoteServiceE2EStub extends DeviceRemoteService {

    @Override
    public ResponseEntity<String> rebootBox(String boxIp) {
        return ResponseEntity.ok("{\"success\":true,\"message\":\"reboot scheduled\"}");
    }

    @Override
    public ResponseEntity<Map> getStreamPreview(String boxIp, String channelId) {
        Map<String, Object> body = new HashMap<>();
        body.put("streamUrl", "ws://localhost:8080/ws/stream/" + channelId);
        body.put("channelId", channelId);
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<String> syncChannelConfig(String boxIp, com.vdc.platform.entity.Channel channel) {
        return ResponseEntity.ok("{\"success\":true,\"message\":\"channel config synced\"}");
    }
}
