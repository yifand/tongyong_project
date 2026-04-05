package com.vdc.pdi.algorithminlet.validator;

import com.vdc.pdi.algorithminlet.dto.request.AlarmEventRequest;
import com.vdc.pdi.algorithminlet.dto.request.StateStreamRequest;
import com.vdc.pdi.algorithminlet.exception.InletException;
import com.vdc.pdi.common.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 请求校验器
 * 负责业务层面的数据校验
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestValidator {

    /**
     * 校验状态流请求
     *
     * @param request 状态流请求
     */
    public void validateStateStream(StateStreamRequest request) {
        // 校验状态码与三元组一致性
        int expectedCode = calculateStateCode(request.getState());
        if (!request.getStateCode().equals(expectedCode)) {
            log.warn("状态码不一致: 期望={}, 实际={}", expectedCode, request.getStateCode());
            throw new InletException(ResultCode.VALIDATION_ERROR,
                    String.format("状态码不一致: 期望%d, 实际%d", expectedCode, request.getStateCode()));
        }

        // 校验时间戳合理性 (不能是未来时间，不能是过去太久)
        validateTimestamp(request.getTimestamp());
    }

    /**
     * 校验报警事件请求
     *
     * @param request 报警事件请求
     */
    public void validateAlarmEvent(AlarmEventRequest request) {
        // 校验时间戳
        validateTimestamp(request.getTimestamp());

        // 校验图片URL格式
        if (StringUtils.isNotBlank(request.getImageUrl())) {
            validateImageUrl(request.getImageUrl());
        }
    }

    /**
     * 计算状态码
     * 根据状态三元组计算对应的状态码
     */
    private int calculateStateCode(StateStreamRequest.StateTriple state) {
        int code = 0;
        if (state.getDoorOpen() == 1) code += 4;
        if (state.getPersonPresent() == 1) code += 2;
        if (state.getEnteringExiting() == 1) code += 1;

        // S1=1, S3=3, S5=5, S7=7, S8=8
        return switch (code) {
            case 0 -> 1;  // 000 -> S1
            case 2 -> 3;  // 010 -> S3
            case 4 -> 5;  // 100 -> S5
            case 6 -> 7;  // 110 -> S7
            case 7 -> 8;  // 111 -> S8
            default -> throw new InletException(ResultCode.VALIDATION_ERROR, "无效的状态组合: " + code);
        };
    }

    /**
     * 校验时间戳
     */
    private void validateTimestamp(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();

        // 不能是未来时间（允许5秒时钟偏移）
        if (timestamp.isAfter(now.plusSeconds(5))) {
            throw new InletException(ResultCode.VALIDATION_ERROR, "时间戳不能是未来时间");
        }

        // 不能是太久以前（10分钟）
        if (timestamp.isBefore(now.minusMinutes(10))) {
            throw new InletException(ResultCode.VALIDATION_ERROR, "时间戳已过期");
        }
    }

    /**
     * 校验图片URL
     */
    private void validateImageUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new InletException(ResultCode.VALIDATION_ERROR, "图片URL格式错误，必须以http://或https://开头");
        }
    }
}
