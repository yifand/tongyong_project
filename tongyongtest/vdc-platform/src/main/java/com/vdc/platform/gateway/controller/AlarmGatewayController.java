package com.vdc.platform.gateway.controller;

import com.vdc.platform.common.ApiResult;
import com.vdc.platform.gateway.dto.BoxAlarmRequest;
import com.vdc.platform.gateway.service.AlarmIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway/v1/box")
@RequiredArgsConstructor
public class AlarmGatewayController {

    private final AlarmIngestionService alarmIngestionService;

    @PostMapping("/alarm")
    public ApiResult<Void> receiveAlarm(@RequestBody BoxAlarmRequest request) {
        alarmIngestionService.ingest(request);
        return ApiResult.success();
    }
}
