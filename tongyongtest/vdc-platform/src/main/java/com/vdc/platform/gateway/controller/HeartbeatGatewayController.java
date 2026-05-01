package com.vdc.platform.gateway.controller;

import com.vdc.platform.common.ApiResult;
import com.vdc.platform.gateway.dto.BoxHeartbeatRequest;
import com.vdc.platform.gateway.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway/v1/box")
@RequiredArgsConstructor
public class HeartbeatGatewayController {

    private final HeartbeatService heartbeatService;

    @PostMapping("/heartbeat")
    public ApiResult<Void> receiveHeartbeat(@RequestBody BoxHeartbeatRequest request) {
        heartbeatService.process(request);
        return ApiResult.success();
    }
}
