package com.vdc.platform.gateway.controller;

import com.vdc.platform.common.ApiResult;
import com.vdc.platform.gateway.dto.BoxStateRequest;
import com.vdc.platform.gateway.service.StateIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway/v1/box")
@RequiredArgsConstructor
public class StateGatewayController {

    private final StateIngestionService stateIngestionService;

    @PostMapping("/state")
    public ApiResult<Void> receiveState(@RequestBody BoxStateRequest request) {
        stateIngestionService.ingest(request);
        return ApiResult.success();
    }
}
