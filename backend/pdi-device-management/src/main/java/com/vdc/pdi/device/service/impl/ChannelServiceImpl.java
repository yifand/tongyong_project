package com.vdc.pdi.device.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.vdc.pdi.common.dto.PageResponse;
import com.vdc.pdi.common.enums.ResultCode;
import com.vdc.pdi.common.exception.BusinessException;
import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.entity.QChannel;
import com.vdc.pdi.device.domain.repository.ChannelRepository;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import com.vdc.pdi.device.dto.request.ChannelQueryRequest;
import com.vdc.pdi.device.dto.request.ChannelRequest;
import com.vdc.pdi.device.dto.response.ChannelResponse;
import com.vdc.pdi.device.mapper.ChannelMapper;
import com.vdc.pdi.device.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通道服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final EdgeBoxRepository edgeBoxRepository;
    private final ChannelMapper channelMapper;

    @Override
    public PageResponse<ChannelResponse> listChannels(ChannelQueryRequest request, Long currentSiteId) {
        QChannel qChannel = QChannel.channel;
        BooleanBuilder predicate = new BooleanBuilder();

        // 站点过滤
        if (request.getSiteId() != null) {
            predicate.and(qChannel.siteId.eq(request.getSiteId()));
        } else if (currentSiteId != null) {
            predicate.and(qChannel.siteId.eq(currentSiteId));
        }

        // 盒子过滤
        if (request.getBoxId() != null) {
            predicate.and(qChannel.boxId.eq(request.getBoxId()));
        }

        // 算法类型过滤
        if (StringUtils.hasText(request.getAlgorithmType())) {
            predicate.and(qChannel.algorithmType.eq(request.getAlgorithmType()));
        }

        // 状态过滤
        if (request.getStatus() != null) {
            predicate.and(qChannel.status.eq(request.getStatus()));
        }

        // 关键字搜索（名称）
        if (StringUtils.hasText(request.getKeyword())) {
            predicate.and(qChannel.name.containsIgnoreCase(request.getKeyword()));
        }

        // 未删除
        predicate.and(qChannel.deletedAt.isNull());

        // 分页
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Channel> channelPage = channelRepository.findAll(predicate, pageable);

        // 批量查询盒子名称
        List<Long> boxIds = channelPage.getContent().stream()
                .map(Channel::getBoxId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, String> boxNameMap;
        if (!boxIds.isEmpty()) {
            List<EdgeBox> boxes = edgeBoxRepository.findAllById(boxIds);
            boxNameMap = boxes.stream()
                    .collect(Collectors.toMap(EdgeBox::getId, EdgeBox::getName));
        } else {
            boxNameMap = new HashMap<>();
        }

        List<ChannelResponse> responses = channelMapper.toResponseList(channelPage.getContent());
        // 设置盒子名称
        responses.forEach(resp -> resp.setBoxName(boxNameMap.getOrDefault(resp.getBoxId(), "")));

        return PageResponse.of(responses, channelPage.getTotalElements(), request.getPage(), size);
    }

    @Override
    public ChannelResponse getChannel(Long id, Long currentSiteId) {
        Channel channel = getChannelAndCheckPermission(id, currentSiteId);
        ChannelResponse response = channelMapper.toResponse(channel);

        // 设置盒子名称
        edgeBoxRepository.findByIdAndDeletedAtIsNull(channel.getBoxId())
                .ifPresent(box -> response.setBoxName(box.getName()));

        return response;
    }

    @Override
    @Transactional
    public Long createChannel(ChannelRequest request, Long currentSiteId, Long currentUserId) {
        // 验证盒子存在
        EdgeBox box = edgeBoxRepository.findByIdAndDeletedAtIsNull(request.getBoxId())
                .orElseThrow(() -> new BusinessException(ResultCode.BIZ_ERROR, "所属盒子不存在"));

        // 验证站点一致
        if (!box.getSiteId().equals(request.getSiteId())) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "盒子不属于该站点");
        }

        Channel channel = channelMapper.toEntity(request);
        channel.setStatus(0); // 离线
        channel.setCreatedBy(currentUserId);

        Channel savedChannel = channelRepository.save(channel);
        log.info("创建通道成功: id={}, name={}", savedChannel.getId(), savedChannel.getName());

        return savedChannel.getId();
    }

    @Override
    @Transactional
    public void updateChannel(Long id, ChannelRequest request, Long currentSiteId) {
        Channel channel = getChannelAndCheckPermission(id, currentSiteId);

        // 如果盒子ID变更，验证新盒子存在
        if (!channel.getBoxId().equals(request.getBoxId())) {
            EdgeBox box = edgeBoxRepository.findByIdAndDeletedAtIsNull(request.getBoxId())
                    .orElseThrow(() -> new BusinessException(ResultCode.BIZ_ERROR, "所属盒子不存在"));

            if (!box.getSiteId().equals(request.getSiteId())) {
                throw new BusinessException(ResultCode.BIZ_ERROR, "盒子不属于该站点");
            }
        }

        channelMapper.updateEntity(request, channel);
        channelRepository.save(channel);
        log.info("更新通道成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteChannel(Long id, Long currentSiteId) {
        Channel channel = getChannelAndCheckPermission(id, currentSiteId);

        channel.setDeletedAt(LocalDateTime.now());
        channelRepository.save(channel);

        log.info("删除通道成功: id={}, name={}", id, channel.getName());
    }

    @Override
    public List<ChannelResponse> getChannelsByBoxId(Long boxId, Long currentSiteId) {
        // 验证盒子存在且有权限
        EdgeBox box = edgeBoxRepository.findByIdAndDeletedAtIsNull(boxId)
                .orElseThrow(() -> new BusinessException(ResultCode.BIZ_ERROR, "盒子不存在"));

        if (currentSiteId != null && !currentSiteId.equals(box.getSiteId())) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "无权访问该盒子");
        }

        List<Channel> channels = channelRepository.findByBoxIdAndDeletedAtIsNull(boxId);
        return channelMapper.toResponseList(channels);
    }

    @Override
    public Channel getChannelById(Long id) {
        return channelRepository.findByIdAndDeletedAtIsNull(id)
                .orElse(null);
    }

    @Override
    public List<Channel> getChannelsByBoxIdInternal(Long boxId) {
        return channelRepository.findByBoxIdAndDeletedAtIsNull(boxId);
    }

    @Override
    @Transactional
    public void updateChannelStatus(Long channelId, Integer status) {
        Channel channel = channelRepository.findByIdAndDeletedAtIsNull(channelId)
                .orElse(null);

        if (channel == null) {
            log.warn("通道不存在: channelId={}", channelId);
            return;
        }

        channel.setStatus(status);
        channelRepository.save(channel);
        log.debug("更新通道状态: channelId={}, status={}", channelId, status);
    }

    /**
     * 获取通道并检查权限
     */
    private Channel getChannelAndCheckPermission(Long id, Long currentSiteId) {
        Channel channel = channelRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ResultCode.BIZ_ERROR, "通道不存在"));

        // 检查站点权限（非超级管理员需要检查）
        if (currentSiteId != null && !currentSiteId.equals(channel.getSiteId())) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "无权访问该通道");
        }

        return channel;
    }
}
