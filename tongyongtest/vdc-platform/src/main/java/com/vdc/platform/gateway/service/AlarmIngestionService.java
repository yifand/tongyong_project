package com.vdc.platform.gateway.service;

import com.vdc.platform.common.MinioStorageService;
import com.vdc.platform.entity.Alarm;
import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.gateway.dto.BoxAlarmRequest;
import com.vdc.platform.service.IAlarmService;
import com.vdc.platform.service.IChannelService;
import com.vdc.platform.service.IEdgeBoxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmIngestionService {

    private final IAlarmService alarmService;
    private final IEdgeBoxService edgeBoxService;
    private final IChannelService channelService;
    private final MinioStorageService minioStorageService;
    private final StringRedisTemplate stringRedisTemplate;

    public void ingest(BoxAlarmRequest request) {
        String eventType = request.getEventType();
        String alarmType = mapAlarmType(eventType);

        EdgeBox box = edgeBoxService.lambdaQuery()
                .eq(EdgeBox::getBoxId, request.getBoxId())
                .one();
        Channel channel = channelService.lambdaQuery()
                .eq(Channel::getChannelId, request.getChannelId())
                .one();

        Long siteId = box != null ? box.getSiteId() : null;
        Long channelDbId = channel != null ? channel.getId() : null;

        String targetPath = null;
        String scenePath = null;
        if (request.getSnapshot() != null) {
            CompletableFuture<String> targetFuture = null;
            CompletableFuture<String> sceneFuture = null;
            if (request.getSnapshot().getTarget() != null && !request.getSnapshot().getTarget().isBlank()) {
                targetFuture = asyncUpload(request.getSnapshot().getTarget(), "alarm/target");
            }
            if (request.getSnapshot().getScene() != null && !request.getSnapshot().getScene().isBlank()) {
                sceneFuture = asyncUpload(request.getSnapshot().getScene(), "alarm/scene");
            }
            if (targetFuture != null) {
                try {
                    targetPath = targetFuture.get();
                } catch (Exception e) {
                    log.error("Async upload target failed", e);
                }
            }
            if (sceneFuture != null) {
                try {
                    scenePath = sceneFuture.get();
                } catch (Exception e) {
                    log.error("Async upload scene failed", e);
                }
            }
        }

        Alarm alarm = new Alarm();
        alarm.setAlarmType(alarmType);
        alarm.setSiteId(siteId);
        alarm.setChannelId(channelDbId);
        alarm.setAlarmTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(request.getTimestamp()), ZoneId.systemDefault()));
        alarm.setProcessStatus("UNPROCESSED");
        alarm.setTargetImage(targetPath);
        alarm.setSceneImage(scenePath);
        alarm.setDescription(eventType);

        alarmService.save(alarm);
        stringRedisTemplate.convertAndSend("vdc:alarm:realtime", String.valueOf(alarm.getId()));
    }

    private String mapAlarmType(String eventType) {
        if ("SMOKE_DETECTED".equalsIgnoreCase(eventType)) {
            return "SMOKE";
        }
        return "PDI_UNQUALIFIED";
    }

    @Async
    public CompletableFuture<String> asyncUpload(String base64Data, String prefix) {
        try {
            String path = minioStorageService.uploadBase64Image(base64Data, prefix);
            return CompletableFuture.completedFuture(path);
        } catch (Exception e) {
            log.error("MinIO upload failed for prefix {}", prefix, e);
            return CompletableFuture.completedFuture(null);
        }
    }
}
