package com.vdc.platform.gateway.service;

import com.vdc.platform.common.MinioStorageService;
import com.vdc.platform.entity.StateStream;
import com.vdc.platform.gateway.dto.BoxStateRequest;
import com.vdc.platform.ruleengine.core.RuleEngine;
import com.vdc.platform.service.IStateStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateIngestionService {

    private final IStateStreamService stateStreamService;
    private final MinioStorageService minioStorageService;
    private final RuleEngine ruleEngine;

    public void ingest(BoxStateRequest request) {
        BoxStateRequest.States states = request.getStates();
        int combo = 1;
        if (Boolean.TRUE.equals(states.getVehiclePresent())) combo += 8;
        if (Boolean.TRUE.equals(states.getDoorOpen())) combo += 4;
        if (Boolean.TRUE.equals(states.getPersonPresent())) combo += 2;
        if (Boolean.TRUE.equals(states.getPersonEnteringExiting())) combo += 1;

        String targetPath = null;
        String scenePath = null;
        if (request.getSnapshot() != null) {
            CompletableFuture<String> targetFuture = null;
            CompletableFuture<String> sceneFuture = null;
            if (request.getSnapshot().getTarget() != null && !request.getSnapshot().getTarget().isBlank()) {
                targetFuture = asyncUpload(request.getSnapshot().getTarget(), "state/target");
            }
            if (request.getSnapshot().getScene() != null && !request.getSnapshot().getScene().isBlank()) {
                sceneFuture = asyncUpload(request.getSnapshot().getScene(), "state/scene");
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

        StateStream record = new StateStream();
        record.setBoxId(request.getBoxId());
        record.setChannelId(request.getChannelId());
        record.setTs(LocalDateTime.ofInstant(Instant.ofEpochSecond(request.getTimestamp()), ZoneId.systemDefault()));
        record.setDoorOpen(states.getDoorOpen());
        record.setPersonPresent(states.getPersonPresent());
        record.setPersonEnteringExiting(states.getPersonEnteringExiting());
        record.setVehiclePresent(states.getVehiclePresent());
        record.setStateCombination(combo);
        record.setSnapshotTarget(targetPath);
        record.setSnapshotScene(scenePath);

        stateStreamService.save(record);
        ruleEngine.process(record);
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
