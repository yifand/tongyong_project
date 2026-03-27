package com.pdi.service.device;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdi.common.enums.ChannelTypeEnum;
import com.pdi.common.enums.DeviceStatusEnum;
import com.pdi.common.enums.StatusEnum;
import com.pdi.common.exception.BusinessException;
import com.pdi.common.result.PageResult;
import com.pdi.common.result.ResultCode;
import com.pdi.dao.entity.Box;
import com.pdi.dao.entity.Channel;
import com.pdi.dao.entity.Site;
import com.pdi.dao.mapper.BoxMapper;
import com.pdi.dao.mapper.ChannelMapper;
import com.pdi.dao.mapper.SiteMapper;
import com.pdi.service.device.dto.BoxDTO;
import com.pdi.service.device.dto.BoxQueryDTO;
import com.pdi.service.device.dto.ChannelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 盒子设备服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class BoxServiceImpl extends ServiceImpl<BoxMapper, Box> implements BoxService {

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BOX_STATUS_KEY = "box:status:";
    private static final String BOX_METRICS_KEY = "box:metrics:";
    private static final long HEARTBEAT_TIMEOUT = 60;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BoxDTO createBox(BoxDTO dto) {
        // 检查点位是否存在
        Site site = siteMapper.selectById(dto.getSiteId());
        if (site == null) {
            throw new BusinessException("所属点位不存在");
        }

        // 检查编码唯一性
        if (lambdaQuery().eq(Box::getBoxCode, dto.getBoxCode()).exists()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "盒子编码已存在");
        }

        Box box = new Box();
        BeanUtils.copyProperties(dto, box);
        box.setStatus(DeviceStatusEnum.OFFLINE.getCode());
        box.setCreatedAt(LocalDateTime.now());
        box.setUpdatedAt(LocalDateTime.now());

        save(box);

        return convertToDTO(box);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBox(Long boxId, BoxDTO dto) {
        Box box = getById(boxId);
        if (box == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "盒子不存在");
        }

        // 检查编码唯一性
        if (StringUtils.hasText(dto.getBoxCode()) && !dto.getBoxCode().equals(box.getBoxCode())) {
            if (lambdaQuery().eq(Box::getBoxCode, dto.getBoxCode()).exists()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "盒子编码已存在");
            }
        }

        BeanUtils.copyProperties(dto, box, "id", "status", "createdAt", "createdBy");
        box.setId(boxId);
        box.setUpdatedAt(LocalDateTime.now());

        updateById(box);
        log.info("更新盒子信息: boxId={}", boxId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBox(Long boxId) {
        Box box = getById(boxId);
        if (box == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "盒子不存在");
        }

        // 删除关联的通道
        channelMapper.delete(new LambdaQueryWrapper<Channel>().eq(Channel::getBoxId, boxId));

        removeById(boxId);
        log.info("删除盒子: boxId={}", boxId);
    }

    @Override
    public BoxDTO getBox(Long boxId) {
        Box box = getById(boxId);
        if (box == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "盒子不存在");
        }

        return convertToDTO(box);
    }

    @Override
    public PageResult<BoxDTO> listBoxes(BoxQueryDTO query) {
        Page<Box> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Box> wrapper = new LambdaQueryWrapper<>();

        if (query.getSiteId() != null) {
            wrapper.eq(Box::getSiteId, query.getSiteId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Box::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Box::getBoxCode, query.getKeyword())
                    .or()
                    .like(Box::getBoxName, query.getKeyword()));
        }

        wrapper.orderByDesc(Box::getCreatedAt);
        Page<Box> pageResult = page(pageParam, wrapper);

        List<BoxDTO> list = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBoxStatus(Long boxId, Integer status) {
        Box box = getById(boxId);
        if (box == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "盒子不存在");
        }

        box.setStatus(status);
        box.setUpdatedAt(LocalDateTime.now());
        updateById(box);

        log.info("更新盒子状态: boxId={}, status={}", boxId, status);
    }

    @Override
    public String rebootBox(Long boxId) {
        Box box = getById(boxId);
        if (box == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "盒子不存在");
        }

        if (box.getStatus() == DeviceStatusEnum.OFFLINE.getCode()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "盒子离线，无法重启");
        }

        String commandId = "CMD" + System.currentTimeMillis();
        log.info("发送重启指令: boxId={}, commandId={}", boxId, commandId);

        // TODO: 通过MQTT或HTTP向盒子发送重启指令

        return commandId;
    }

    @Override
    public BoxDTO getBoxMetrics(Long boxId) {
        Box box = getById(boxId);
        if (box == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "盒子不存在");
        }

        // 从Redis获取实时指标
        String metricsKey = BOX_METRICS_KEY + boxId;
        Object metrics = redisTemplate.opsForValue().get(metricsKey);

        BoxDTO dto = convertToDTO(box);
        if (metrics != null) {
            // 合并实时指标
            BeanUtils.copyProperties(metrics, dto);
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleHeartbeat(Long boxId, BoxDTO dto) {
        Box box = getById(boxId);
        if (box == null) {
            log.warn("收到未知盒子的心跳: boxId={}", boxId);
            return;
        }

        // 更新盒子状态为在线
        box.setStatus(DeviceStatusEnum.ONLINE.getCode());
        box.setLastHeartbeat(LocalDateTime.now());
        box.setUpdatedAt(LocalDateTime.now());
        updateById(box);

        // 缓存实时指标到Redis
        String metricsKey = BOX_METRICS_KEY + boxId;
        redisTemplate.opsForValue().set(metricsKey, dto, HEARTBEAT_TIMEOUT, TimeUnit.SECONDS);

        log.debug("处理心跳: boxId={}", boxId);
    }

    // ==================== 通道管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelDTO createChannel(ChannelDTO dto) {
        // 检查盒子是否存在
        Box box = getById(dto.getBoxId());
        if (box == null) {
            throw new BusinessException("所属盒子不存在");
        }

        // 检查通道编码唯一性
        if (channelMapper.exists(new LambdaQueryWrapper<Channel>()
                .eq(Channel::getChannelCode, dto.getChannelCode()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "通道编码已存在");
        }

        Channel channel = new Channel();
        BeanUtils.copyProperties(dto, channel);
        channel.setSiteId(box.getSiteId());
        channel.setStatus(StatusEnum.ENABLED.getCode());
        channel.setCreatedAt(LocalDateTime.now());
        channel.setUpdatedAt(LocalDateTime.now());

        channelMapper.insert(channel);

        return convertToChannelDTO(channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannel(Long channelId, ChannelDTO dto) {
        Channel channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "通道不存在");
        }

        BeanUtils.copyProperties(dto, channel, "id", "siteId", "createdAt");
        channel.setId(channelId);
        channel.setUpdatedAt(LocalDateTime.now());

        channelMapper.updateById(channel);
        log.info("更新通道信息: channelId={}", channelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(Long channelId) {
        Channel channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "通道不存在");
        }

        channelMapper.deleteById(channelId);
        log.info("删除通道: channelId={}", channelId);
    }

    @Override
    public ChannelDTO getChannel(Long channelId) {
        Channel channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "通道不存在");
        }

        return convertToChannelDTO(channel);
    }

    @Override
    public PageResult<ChannelDTO> listChannels(BoxQueryDTO query) {
        Page<Channel> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();

        if (query.getBoxId() != null) {
            wrapper.eq(Channel::getBoxId, query.getBoxId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(Channel::getSiteId, query.getSiteId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Channel::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Channel::getChannelCode, query.getKeyword())
                    .or()
                    .like(Channel::getChannelName, query.getKeyword()));
        }

        wrapper.orderByAsc(Channel::getSortOrder);
        Page<Channel> pageResult = channelMapper.selectPage(pageParam, wrapper);

        List<ChannelDTO> list = pageResult.getRecords().stream()
                .map(this::convertToChannelDTO)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    public List<ChannelDTO> listChannelsByBoxId(Long boxId) {
        List<Channel> channels = channelMapper.selectList(
                new LambdaQueryWrapper<Channel>()
                        .eq(Channel::getBoxId, boxId)
                        .orderByAsc(Channel::getSortOrder));

        return channels.stream()
                .map(this::convertToChannelDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannelStatus(Long channelId, Integer status) {
        Channel channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "通道不存在");
        }

        channel.setStatus(status);
        channel.setUpdatedAt(LocalDateTime.now());
        channelMapper.updateById(channel);

        log.info("更新通道状态: channelId={}, status={}", channelId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannelAlgorithm(Long channelId, String algorithmConfig) {
        Channel channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "通道不存在");
        }

        try {
            JsonNode configNode = objectMapper.readTree(algorithmConfig);
            channel.setAlgorithmConfig(configNode);
            channel.setUpdatedAt(LocalDateTime.now());
            channelMapper.updateById(channel);

            log.info("更新通道算法配置: channelId={}", channelId);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "算法配置格式错误");
        }
    }

    // ==================== 私有方法 ====================

    private BoxDTO convertToDTO(Box box) {
        BoxDTO dto = new BoxDTO();
        BeanUtils.copyProperties(box, dto);

        // 设置状态名称
        DeviceStatusEnum statusEnum = DeviceStatusEnum.getByCode(box.getStatus());
        if (statusEnum != null) {
            dto.setStatusName(statusEnum.getName());
        }

        // 查询点位名称
        if (box.getSiteId() != null) {
            Site site = siteMapper.selectById(box.getSiteId());
            if (site != null) {
                dto.setSiteName(site.getSiteName());
            }
        }

        // 查询通道数量
        Long channelCount = channelMapper.selectCount(
                new LambdaQueryWrapper<Channel>().eq(Channel::getBoxId, box.getId()));
        dto.setChannelCount(channelCount.intValue());

        return dto;
    }

    private ChannelDTO convertToChannelDTO(Channel channel) {
        ChannelDTO dto = new ChannelDTO();
        BeanUtils.copyProperties(channel, dto);

        // 设置通道类型名称
        ChannelTypeEnum typeEnum = ChannelTypeEnum.getByCode(channel.getChannelType());
        if (typeEnum != null) {
            dto.setChannelTypeName(typeEnum.getName());
        }

        // 设置状态名称
        StatusEnum statusEnum = StatusEnum.getByCode(channel.getStatus());
        if (statusEnum != null) {
            dto.setStatusName(statusEnum.getDescription());
        }

        // 查询盒子名称
        if (channel.getBoxId() != null) {
            Box box = getById(channel.getBoxId());
            if (box != null) {
                dto.setBoxName(box.getBoxName());
            }
        }

        // 查询站点名称
        if (channel.getSiteId() != null) {
            Site site = siteMapper.selectById(channel.getSiteId());
            if (site != null) {
                dto.setSiteName(site.getSiteName());
            }
        }

        return dto;
    }

}
