package com.vdc.pdi.device.service.impl;

import com.vdc.pdi.common.enums.ResultCode;
import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import com.vdc.pdi.device.domain.vo.BoxStatus;
import com.vdc.pdi.device.dto.response.DeviceOverviewResponse;
import com.vdc.pdi.device.dto.response.SiteDeviceStatusResponse;
import com.vdc.pdi.device.service.DeviceStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备状态服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceStatusServiceImpl implements DeviceStatusService {

    private final EdgeBoxRepository edgeBoxRepository;
    private final ChannelRepository channelRepository;

    @Override
    public DeviceOverviewResponse getOverview(Long siteId, Long currentSiteId, boolean isSuperAdmin) {
        // 确定查询的站点ID
        Long querySiteId = siteId;
        if (querySiteId == null) {
            querySiteId = currentSiteId;
        }

        if (querySiteId == null) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "站点ID不能为空");
        }

        // 查询盒子统计
        long totalBoxes = edgeBoxRepository.countBySiteIdAndDeletedAtIsNull(querySiteId);
        long onlineBoxes = edgeBoxRepository.countBySiteIdAndStatusAndDeletedAtIsNull(querySiteId, 1);
        long offlineBoxes = totalBoxes - onlineBoxes;

        // 查询通道统计
        long totalChannels = channelRepository.countBySiteIdAndDeletedAtIsNull(querySiteId);
        long onlineChannels = channelRepository.countBySiteIdAndStatusAndDeletedAtIsNull(querySiteId, 1);
        long offlineChannels = totalChannels - onlineChannels;

        // 查询算法统计
        List<Object[]> algorithmCounts = channelRepository.countBySiteIdGroupByAlgorithmType(querySiteId);
        Map<String, Long> algorithmCountMap = new HashMap<>();
        for (Object[] row : algorithmCounts) {
            String algorithmType = (String) row[0];
            Long count = (Long) row[1];
            if (algorithmType != null) {
                algorithmCountMap.put(algorithmType, count);
            }
        }

        // 组装响应
        DeviceOverviewResponse response = new DeviceOverviewResponse();
        response.setSiteId(querySiteId);
        response.setSiteName("站点" + querySiteId); // TODO: 从站点服务获取名称

        response.setTotalBoxes((int) totalBoxes);
        response.setOnlineBoxes((int) onlineBoxes);
        response.setOfflineBoxes((int) offlineBoxes);
        response.setBoxOnlineRate(calculateRate(onlineBoxes, totalBoxes));

        response.setTotalChannels((int) totalChannels);
        response.setOnlineChannels((int) onlineChannels);
        response.setOfflineChannels((int) offlineChannels);
        response.setChannelOnlineRate(calculateRate(onlineChannels, totalChannels));

        response.setSmokeChannels(algorithmCountMap.getOrDefault("smoke", 0L).intValue());
        response.setPdiLeftFrontChannels(algorithmCountMap.getOrDefault("pdi_left_front", 0L).intValue());
        response.setPdiLeftRearChannels(algorithmCountMap.getOrDefault("pdi_left_rear", 0L).intValue());
        response.setPdiSlideChannels(algorithmCountMap.getOrDefault("pdi_slide", 0L).intValue());

        response.setUpdateTime(LocalDateTime.now());

        return response;
    }

    @Override
    public List<SiteDeviceStatusResponse> getAllSitesStatus(Long currentSiteId, boolean isSuperAdmin) {
        // TODO: 超级管理员查询所有站点，普通用户只能查看自己站点
        // 这里简化处理，返回所有有盒子的站点

        List<EdgeBox> allBoxes = edgeBoxRepository.findAllByDeletedAtIsNull();

        // 按站点分组统计
        Map<Long, SiteStats> statsMap = new HashMap<>();

        for (EdgeBox box : allBoxes) {
            SiteStats stats = statsMap.computeIfAbsent(box.getSiteId(), id -> {
                SiteStats s = new SiteStats();
                s.siteId = id;
                s.siteName = "站点" + id; // TODO: 从站点服务获取名称
                return s;
            });

            stats.totalBoxes++;
            if (box.isOnline()) {
                stats.onlineBoxes++;
            } else {
                stats.offlineBoxes++;
            }
        }

        // 统计通道
        List<com.vdc.pdi.device.domain.entity.Channel> allChannels = channelRepository.findAll();
        for (com.vdc.pdi.device.domain.entity.Channel channel : allChannels) {
            if (channel.getDeletedAt() != null) {
                continue;
            }

            SiteStats stats = statsMap.get(channel.getSiteId());
            if (stats == null) {
                continue;
            }

            stats.totalChannels++;
            if (channel.isOnline()) {
                stats.onlineChannels++;
            } else {
                stats.offlineChannels++;
            }
        }

        // 组装响应
        List<SiteDeviceStatusResponse> responses = new ArrayList<>();
        for (SiteStats stats : statsMap.values()) {
            SiteDeviceStatusResponse response = new SiteDeviceStatusResponse();
            response.setSiteId(stats.siteId);
            response.setSiteName(stats.siteName);
            response.setTotalBoxes(stats.totalBoxes);
            response.setOnlineBoxes(stats.onlineBoxes);
            response.setOfflineBoxes(stats.offlineBoxes);
            response.setBoxOnlineRate(calculateRate(stats.onlineBoxes, stats.totalBoxes));
            response.setTotalChannels(stats.totalChannels);
            response.setOnlineChannels(stats.onlineChannels);
            response.setOfflineChannels(stats.offlineChannels);
            response.setChannelOnlineRate(calculateRate(stats.onlineChannels, stats.totalChannels));

            responses.add(response);
        }

        return responses;
    }

    @Override
    public BoxStatus getBoxStatus(Long boxId) {
        EdgeBox box = edgeBoxRepository.findByIdAndDeletedAtIsNull(boxId)
                .orElse(null);

        if (box == null) {
            return null;
        }

        return BoxStatus.builder()
                .boxId(boxId)
                .online(box.isOnline())
                .lastHeartbeatAt(box.getLastHeartbeatAt())
                .timeoutSeconds(60) // 从配置读取
                .build();
    }

    /**
     * 计算比率
     */
    private Double calculateRate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(numerator * 100.0 / denominator)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 站点统计数据
     */
    private static class SiteStats {
        Long siteId;
        String siteName;
        int totalBoxes;
        int onlineBoxes;
        int offlineBoxes;
        int totalChannels;
        int onlineChannels;
        int offlineChannels;
    }
}
