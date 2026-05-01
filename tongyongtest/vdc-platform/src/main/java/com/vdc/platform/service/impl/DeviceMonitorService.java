package com.vdc.platform.service.impl;

import com.vdc.platform.entity.Alarm;
import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.service.IAlarmService;
import com.vdc.platform.service.IChannelService;
import com.vdc.platform.service.IEdgeBoxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceMonitorService {

    private final IEdgeBoxService edgeBoxService;
    private final IChannelService channelService;
    private final IAlarmService alarmService;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 30000)
    public void checkHeartbeatTimeout() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(90);

        List<EdgeBox> offlineBoxes = edgeBoxService.lambdaQuery()
                .eq(EdgeBox::getStatus, 1)
                .lt(EdgeBox::getLastHeartbeat, threshold)
                .list();

        for (EdgeBox box : offlineBoxes) {
            EdgeBox update = new EdgeBox();
            update.setId(box.getId());
            update.setStatus(0);
            edgeBoxService.updateById(update);
            log.info("Box {} marked offline due to heartbeat timeout", box.getBoxId());

            createOfflineAlarm(box);

            List<Channel> channels = channelService.lambdaQuery()
                    .eq(Channel::getBoxId, box.getId())
                    .eq(Channel::getStatus, 1)
                    .list();
            for (Channel ch : channels) {
                Channel chUpdate = new Channel();
                chUpdate.setId(ch.getId());
                chUpdate.setStatus(0);
                channelService.updateById(chUpdate);
            }
        }
    }

    private void createOfflineAlarm(EdgeBox box) {
        Alarm alarm = new Alarm();
        alarm.setAlarmType("DEVICE_OFFLINE");
        alarm.setSiteId(box.getSiteId());
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setProcessStatus("UNPROCESSED");
        alarm.setDescription("设备离线: " + box.getBoxName() + " (" + box.getBoxId() + ")");
        alarmService.save(alarm);
        stringRedisTemplate.convertAndSend("vdc:alarm:realtime", String.valueOf(alarm.getId()));
        log.info("Created DEVICE_OFFLINE alarm for box {}", box.getBoxId());
    }
}
