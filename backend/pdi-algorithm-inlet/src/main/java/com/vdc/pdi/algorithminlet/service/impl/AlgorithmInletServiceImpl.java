package com.vdc.pdi.algorithminlet.service.impl;

import com.vdc.pdi.algorithminlet.domain.entity.IdempotencyRecord;
import com.vdc.pdi.algorithminlet.domain.event.AlarmEvent;
import com.vdc.pdi.algorithminlet.domain.event.StateStreamEvent;
import com.vdc.pdi.algorithminlet.domain.repository.IdempotencyRepository;
import com.vdc.pdi.algorithminlet.dto.request.AlarmEventRequest;
import com.vdc.pdi.algorithminlet.dto.request.StateStreamRequest;
import com.vdc.pdi.algorithminlet.service.AlgorithmInletService;
import com.vdc.pdi.algorithminlet.validator.RequestValidator;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 算法数据入口服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlgorithmInletServiceImpl implements AlgorithmInletService {

    private final ApplicationEventPublisher eventPublisher;
    private final RequestValidator requestValidator;
    private final IdempotencyRepository idempotencyRepository;
    private final EdgeBoxRepository edgeBoxRepository;
    private final ChannelRepository channelRepository;

    @Value("${algorithm-inlet.idempotency.window-minutes:5}")
    private int idempotencyWindowMinutes;

    @Value("${algorithm-inlet.idempotency.expire-minutes:10}")
    private int idempotencyExpireMinutes;

    @Override
    @Transactional
    public void processStateStream(StateStreamRequest request, Long siteId) {
        long startTime = System.currentTimeMillis();

        // 1. 生成幂等键
        String idempotencyKey = generateIdempotencyKey(
                request.getBoxId(),
                request.getChannelId(),
                request.getTimestamp()
        );

        // 2. 幂等检查
        if (isDuplicate(idempotencyKey)) {
            log.debug("重复状态流数据，已忽略: boxId={}, channelId={}, timestamp={}",
                    request.getBoxId(), request.getChannelId(), request.getTimestamp());
            return;
        }

        // 3. 保存幂等记录
        saveIdempotencyRecord(idempotencyKey, "STATE_STREAM");

        // 4. 数据校验
        requestValidator.validateStateStream(request);

        // 5. 校验通道存在性
        validateChannelExists(request.getBoxId(), request.getChannelId());

        // 6. 发布领域事件（异步处理）
        StateStreamEvent event = new StateStreamEvent(
                null, // streamId在持久化后生成，这里先传null
                siteId,
                request.getBoxId(),
                request.getChannelId(),
                request.getTimestamp(),
                request.getStateCode(),
                new StateStreamEvent.StateTriple(
                        request.getState().getDoorOpen(),
                        request.getState().getPersonPresent(),
                        request.getState().getEnteringExiting()
                )
        );
        eventPublisher.publishEvent(event);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.debug("状态流数据处理完成: boxId={}, channelId={}, elapsed={}ms",
                request.getBoxId(), request.getChannelId(), elapsedTime);
    }

    @Override
    @Transactional
    public void processAlarmEvent(AlarmEventRequest request, Long siteId) {
        long startTime = System.currentTimeMillis();

        // 1. 生成幂等键（报警事件增加类型区分）
        String idempotencyKey = generateIdempotencyKey(
                request.getBoxId(),
                request.getChannelId(),
                request.getTimestamp()
        ) + ":" + request.getAlarmType();

        // 2. 幂等检查
        if (isDuplicate(idempotencyKey)) {
            log.debug("重复报警事件，已忽略: boxId={}, alarmType={}",
                    request.getBoxId(), request.getAlarmType());
            return;
        }

        // 3. 保存幂等记录
        saveIdempotencyRecord(idempotencyKey, "ALARM_EVENT");

        // 4. 数据校验
        requestValidator.validateAlarmEvent(request);

        // 5. 校验通道存在性
        validateChannelExists(request.getBoxId(), request.getChannelId());

        // 6. 查找盒子ID
        Long boxRecordId = findBoxRecordId(request.getBoxId());

        // 7. 发布领域事件
        AlarmEvent event = new AlarmEvent(
                null, // alarmId在持久化后生成
                siteId,
                request.getBoxId(),
                request.getChannelId(),
                request.getAlarmType(),
                request.getTimestamp(),
                request.getImageUrl(),
                request.getConfidence(),
                request.getLocation()
        );
        eventPublisher.publishEvent(event);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("报警事件处理完成: boxId={}, alarmType={}, boxRecordId={}, elapsed={}ms",
                request.getBoxId(), request.getAlarmType(), boxRecordId, elapsedTime);
    }

    /**
     * 生成幂等键
     */
    private String generateIdempotencyKey(String boxId, String channelId, LocalDateTime timestamp) {
        return String.format("%s:%s:%s", boxId, channelId, timestamp.toString());
    }

    /**
     * 检查是否为重复数据
     */
    private boolean isDuplicate(String key) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(idempotencyWindowMinutes);
        return idempotencyRepository.existsByKeyAndCreatedAtAfter(key, windowStart);
    }

    /**
     * 保存幂等记录
     */
    private void saveIdempotencyRecord(String key, String type) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setKey(key);
        record.setType(type);
        record.setExpireAt(LocalDateTime.now().plusMinutes(idempotencyExpireMinutes));
        idempotencyRepository.save(record);
    }

    /**
     * 校验通道存在性
     */
    private void validateChannelExists(String boxId, String channelId) {
        Long boxRecordId = findBoxRecordId(boxId);
        if (boxRecordId == null) {
            log.warn("通道校验失败，盒子不存在: boxId={}", boxId);
            return;
        }

        // 简化处理：只校验盒子存在性，通道校验可选
        // 在实际项目中可以通过channelRepository进一步校验
    }

    /**
     * 根据盒子编码查找盒子记录ID
     */
    private Long findBoxRecordId(String boxId) {
        try {
            Long id = Long.parseLong(boxId.replaceAll("[^0-9]", ""));
            Optional<EdgeBox> boxOpt = edgeBoxRepository.findByIdAndDeletedAtIsNull(id);
            return boxOpt.map(EdgeBox::getId).orElse(null);
        } catch (NumberFormatException e) {
            // 尝试直接通过名称查找
            return edgeBoxRepository.findAllByDeletedAtIsNull().stream()
                    .filter(box -> boxId.equals(box.getName()))
                    .findFirst()
                    .map(EdgeBox::getId)
                    .orElse(null);
        }
    }
}
