package com.pdi.receiver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdi.common.enums.DeviceStatusEnum;
import com.pdi.dao.entity.Box;
import com.pdi.dao.mapper.BoxMapper;
import com.pdi.receiver.dto.HeartbeatDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 心跳处理服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class HeartbeatServiceImpl implements HeartbeatService {

    private static final String HEARTBEAT_KEY = "heartbeat:";
    private static final long HEARTBEAT_TIMEOUT_MINUTES = 5; // 5分钟未收到心跳视为离线
    private static final long HEARTBEAT_CACHE_MINUTES = 10; // 心跳缓存10分钟

    @Autowired
    private BoxMapper boxMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processHeartbeat(HeartbeatDTO dto) {
        if (dto.getBoxId() == null) {
            log.warn("心跳数据缺少boxId");
            return;
        }

        Long boxId = dto.getBoxId();

        // 1. 更新盒子信息
        Box box = boxMapper.selectById(boxId);
        if (box == null) {
            log.warn("盒子不存在: {}", boxId);
            return;
        }

        // 2. 更新盒子状态和资源使用情况
        Box updateBox = new Box();
        updateBox.setId(boxId);
        updateBox.setLastHeartbeat(LocalDateTime.now());
        
        if (dto.getCpuUsage() != null) {
            updateBox.setCpuUsage(BigDecimal.valueOf(dto.getCpuUsage()));
        }
        if (dto.getMemoryUsage() != null) {
            updateBox.setMemoryUsage(BigDecimal.valueOf(dto.getMemoryUsage()));
        }
        if (dto.getDiskUsage() != null) {
            updateBox.setDiskUsage(BigDecimal.valueOf(dto.getDiskUsage()));
        }
        if (dto.getGpuUsage() != null) {
            updateBox.setGpuUsage(BigDecimal.valueOf(dto.getGpuUsage()));
        }
        if (dto.getSoftwareVersion() != null) {
            updateBox.setSoftwareVersion(dto.getSoftwareVersion());
        }
        if (dto.getAlgorithmVersion() != null) {
            updateBox.setAlgorithmVersion(dto.getAlgorithmVersion());
        }
        
        // 如果盒子原来是离线状态，更新为在线
        if (DeviceStatusEnum.OFFLINE.getCode().equals(box.getStatus())) {
            updateBox.setStatus(DeviceStatusEnum.ONLINE.getCode());
            log.info("盒子恢复在线: boxId={}", boxId);
        }

        boxMapper.updateById(updateBox);

        // 3. 缓存心跳时间
        String key = HEARTBEAT_KEY + boxId;
        redisTemplate.opsForValue().set(key, System.currentTimeMillis(), HEARTBEAT_CACHE_MINUTES, TimeUnit.MINUTES);

        log.debug("心跳已处理: boxId={}, cpu={}%, memory={}%", 
                boxId, dto.getCpuUsage(), dto.getMemoryUsage());
    }

    @Override
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void checkHeartbeatTimeout() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT_MINUTES);
        
        LambdaQueryWrapper<Box> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Box::getStatus, DeviceStatusEnum.ONLINE.getCode())
               .and(w -> w.lt(Box::getLastHeartbeat, timeoutTime)
                         .or().isNull(Box::getLastHeartbeat));
        
        List<Box> offlineBoxes = boxMapper.selectList(wrapper);
        
        for (Box box : offlineBoxes) {
            markBoxOffline(box.getId());
            log.warn("盒子心跳超时，标记为离线: boxId={}, lastHeartbeat={}", 
                    box.getId(), box.getLastHeartbeat());
        }
    }

    @Override
    public void markBoxOnline(Long boxId) {
        Box box = new Box();
        box.setId(boxId);
        box.setStatus(DeviceStatusEnum.ONLINE.getCode());
        box.setLastHeartbeat(LocalDateTime.now());
        boxMapper.updateById(box);
    }

    @Override
    public void markBoxOffline(Long boxId) {
        Box box = new Box();
        box.setId(boxId);
        box.setStatus(DeviceStatusEnum.OFFLINE.getCode());
        boxMapper.updateById(box);
    }

}
