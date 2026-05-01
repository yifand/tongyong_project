package com.vdc.platform.service.impl;

import com.vdc.platform.entity.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceRemoteService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> rebootBox(String boxIp) {
        String url = String.format("http://%s:8080/api/v1/system/reboot", boxIp);
        log.info("Calling reboot API: {}", url);
        return restTemplate.postForEntity(url, null, String.class);
    }

    public ResponseEntity<Map> getStreamPreview(String boxIp, String channelId) {
        String url = String.format("http://%s:8080/api/v1/stream/%s", boxIp, channelId);
        log.info("Calling stream preview API: {}", url);
        return restTemplate.getForEntity(url, Map.class);
    }

    public ResponseEntity<String> syncChannelConfig(String boxIp, Channel channel) {
        String url = String.format("http://%s:8080/api/v1/channel/config", boxIp);
        log.info("Calling sync channel config API: {} for channel {}", url, channel.getChannelId());
        Map<String, Object> body = new HashMap<>();
        body.put("channelId", channel.getChannelId());
        body.put("channelName", channel.getChannelName());
        body.put("channelType", channel.getChannelType());
        body.put("algorithmType", channel.getAlgorithmType());
        body.put("rtspUrl", channel.getRtspUrl());
        body.put("username", channel.getUsername());
        body.put("password", channel.getPassword());
        return restTemplate.postForEntity(url, body, String.class);
    }
}
