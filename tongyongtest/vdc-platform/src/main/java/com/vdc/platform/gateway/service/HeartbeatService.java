package com.vdc.platform.gateway.service;

import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.gateway.dto.BoxHeartbeatRequest;
import com.vdc.platform.service.IChannelService;
import com.vdc.platform.service.IEdgeBoxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private final IEdgeBoxService edgeBoxService;
    private final IChannelService channelService;

    public void process(BoxHeartbeatRequest request) {
        EdgeBox box = edgeBoxService.lambdaQuery()
                .eq(EdgeBox::getBoxId, request.getBoxId())
                .one();
        if (box == null) {
            log.warn("Heartbeat received for unknown box: {}", request.getBoxId());
            return;
        }

        Map<String, Object> sysInfo = request.getSystemInfo();
        BigDecimal cpu = extractDecimal(sysInfo, "cpuUsage");
        BigDecimal mem = extractDecimal(sysInfo, "memUsage");
        BigDecimal disk = extractDecimal(sysInfo, "diskUsage");
        String version = extractString(sysInfo, "version");

        EdgeBox updateBox = new EdgeBox();
        updateBox.setId(box.getId());
        updateBox.setStatus("ONLINE".equalsIgnoreCase(request.getStatus()) ? 1 : 0);
        updateBox.setLastHeartbeat(LocalDateTime.now());
        updateBox.setCpuUsage(cpu);
        updateBox.setMemUsage(mem);
        updateBox.setDiskUsage(disk);
        updateBox.setVersion(version);
        edgeBoxService.updateById(updateBox);

        List<BoxHeartbeatRequest.ChannelStatus> channels = request.getChannels();
        if (channels != null) {
            for (BoxHeartbeatRequest.ChannelStatus cs : channels) {
                Channel channel = channelService.lambdaQuery()
                        .eq(Channel::getChannelId, cs.getChannelId())
                        .eq(Channel::getBoxId, box.getId())
                        .one();
                if (channel != null) {
                    Channel updateChannel = new Channel();
                    updateChannel.setId(channel.getId());
                    updateChannel.setStatus("ONLINE".equalsIgnoreCase(cs.getStatus()) ? 1 : 0);
                    channelService.updateById(updateChannel);
                }
            }
        }
    }

    private BigDecimal extractDecimal(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        }
        if (val instanceof String) {
            try {
                return new BigDecimal((String) val);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String extractString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
