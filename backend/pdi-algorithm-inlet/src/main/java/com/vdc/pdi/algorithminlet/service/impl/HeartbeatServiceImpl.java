package com.vdc.pdi.algorithminlet.service.impl;

import com.vdc.pdi.algorithminlet.domain.event.HeartbeatEvent;
import com.vdc.pdi.algorithminlet.dto.request.HeartbeatRequest;
import com.vdc.pdi.algorithminlet.service.HeartbeatService;
import com.vdc.pdi.common.enums.ResultCode;
import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 心跳服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HeartbeatServiceImpl implements HeartbeatService {

    private final ApplicationEventPublisher eventPublisher;
    private final EdgeBoxRepository edgeBoxRepository;

    @Override
    @Transactional
    public void processHeartbeat(HeartbeatRequest request, Long siteId) {
        long startTime = System.currentTimeMillis();

        // 1. 查找盒子
        EdgeBox box = findBoxByCode(request.getBoxId());
        if (box == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "盒子不存在: " + request.getBoxId());
        }

        // 2. 更新盒子状态
        box.setLastHeartbeatAt(LocalDateTime.now());
        box.setCpuUsage(request.getCpuUsage());
        box.setMemoryUsage(request.getMemoryUsage());
        box.setDiskUsage(request.getDiskUsage());
        box.setVersion(request.getVersion());
        box.setStatus(1); // 在线

        edgeBoxRepository.save(box);

        // 3. 发布心跳事件（用于监控告警）
        HeartbeatEvent event = new HeartbeatEvent(
                box.getId(),
                siteId,
                request.getBoxId(),
                request.getTimestamp(),
                request.getCpuUsage(),
                request.getMemoryUsage(),
                request.getDiskUsage()
        );
        eventPublisher.publishEvent(event);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.debug("心跳处理完成: boxId={}, boxRecordId={}, elapsed={}ms",
                request.getBoxId(), box.getId(), elapsedTime);
    }

    /**
     * 根据盒子编码查找盒子
     */
    private EdgeBox findBoxByCode(String boxId) {
        try {
            Long id = Long.parseLong(boxId.replaceAll("[^0-9]", ""));
            Optional<EdgeBox> boxOpt = edgeBoxRepository.findByIdAndDeletedAtIsNull(id);
            if (boxOpt.isPresent()) {
                return boxOpt.get();
            }
        } catch (NumberFormatException e) {
            log.debug("盒子ID格式非数字: {}", boxId);
        }

        // 尝试通过名称查找
        return edgeBoxRepository.findAllByDeletedAtIsNull().stream()
                .filter(box -> boxId.equals(box.getName()))
                .findFirst()
                .orElse(null);
    }
}
