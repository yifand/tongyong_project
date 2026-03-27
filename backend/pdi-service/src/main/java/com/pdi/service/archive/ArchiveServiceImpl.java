package com.pdi.service.archive;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdi.common.enums.PDITaskResultEnum;
import com.pdi.common.enums.PDITaskStatusEnum;
import com.pdi.common.exception.BusinessException;
import com.pdi.common.result.PageResult;
import com.pdi.common.result.ResultCode;
import com.pdi.dao.entity.*;
import com.pdi.dao.mapper.*;
import com.pdi.service.archive.dto.ArchiveCompleteDTO;
import com.pdi.service.archive.dto.ArchiveQueryDTO;
import com.pdi.service.archive.vo.ArchiveDetailVO;
import com.pdi.service.archive.vo.ArchiveVO;
import com.pdi.service.archive.vo.TimelineEventVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 档案服务实现
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Slf4j
@Service
public class ArchiveServiceImpl extends ServiceImpl<PDITaskMapper, PDITask> implements ArchiveService {

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private BoxMapper boxMapper;

    @Autowired
    private AlarmMapper alarmMapper;

    @Autowired
    private TimelineEventMapper timelineEventMapper;

    private static final String TASK_NO_PREFIX = "PDI";
    private static final int STANDARD_DURATION = 30; // 标准作业时长30分钟

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArchiveVO createArchive(Long channelId) {
        // 检查通道是否存在
        Channel channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BusinessException("通道不存在");
        }

        // 检查是否有进行中的档案
        PDITask existingTask = lambdaQuery()
                .eq(PDITask::getChannelId, channelId)
                .eq(PDITask::getTaskStatus, PDITaskStatusEnum.IN_PROGRESS.getCode())
                .one();
        if (existingTask != null) {
            log.warn("该通道已有进行中的作业: channelId={}, taskId={}", channelId, existingTask.getId());
            return convertToVO(existingTask);
        }

        PDITask task = new PDITask();
        task.setTaskNo(generateTaskNo());
        task.setChannelId(channelId);
        task.setSiteId(channel.getSiteId());
        task.setStartTime(LocalDateTime.now());
        task.setStandardDuration(STANDARD_DURATION * 60); // 转换为秒
        task.setTaskStatus(PDITaskStatusEnum.IN_PROGRESS.getCode());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        save(task);

        // 创建开始时间线事件
        TimelineEvent event = new TimelineEvent();
        event.setTaskId(task.getId());
        event.setEventType(1); // 进入
        event.setEventTime(LocalDateTime.now());
        event.setEventDesc("人员进入检测区域，开始PDI作业");
        timelineEventMapper.insert(event);

        log.info("创建PDI作业档案: taskId={}, taskNo={}", task.getId(), task.getTaskNo());

        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeArchive(Long archiveId, ArchiveCompleteDTO dto) {
        PDITask task = getById(archiveId);
        if (task == null) {
            throw new BusinessException(3001, "档案不存在");
        }

        if (task.getTaskStatus() == PDITaskStatusEnum.COMPLETED.getCode()) {
            throw new BusinessException("档案已完成，不能重复操作");
        }

        // 计算作业时长
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(task.getStartTime(), endTime);
        long seconds = duration.getSeconds();

        task.setEndTime(endTime);
        task.setDurationSeconds((int) seconds);
        task.setTaskStatus(PDITaskStatusEnum.COMPLETED.getCode());
        task.setEnterStateSeq(dto.getEnterStateSeq());
        task.setExitStateSeq(dto.getExitStateSeq());

        // 判定作业结果
        Integer taskResult = determineTaskResult(seconds / 60, dto);
        task.setTaskResult(taskResult);

        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);

        // 创建离开时间线事件
        TimelineEvent event = new TimelineEvent();
        event.setTaskId(archiveId);
        event.setEventType(2); // 离开
        event.setEventTime(endTime);
        event.setEventDesc("人员离开检测区域，PDI作业完成，时长" + (seconds / 60) + "分钟");
        timelineEventMapper.insert(event);

        log.info("完成PDI作业档案: taskId={}, duration={}, result={}", archiveId, seconds, taskResult);
    }

    @Override
    public PageResult<ArchiveVO> listArchives(ArchiveQueryDTO query) {
        Page<PDITask> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<PDITask> wrapper = new LambdaQueryWrapper<>();

        // 站点筛选
        if (query.getSiteId() != null) {
            wrapper.eq(PDITask::getSiteId, query.getSiteId());
        }

        // 通道筛选
        if (query.getChannelId() != null) {
            wrapper.eq(PDITask::getChannelId, query.getChannelId());
        }

        // 日期范围筛选
        if (query.getStartDate() != null) {
            wrapper.ge(PDITask::getStartTime, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(PDITask::getStartTime, query.getEndDate().plusDays(1).atStartOfDay());
        }

        // 作业结果筛选
        if (query.getTaskResult() != null) {
            wrapper.eq(PDITask::getTaskResult, query.getTaskResult());
        }

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(PDITask::getTaskNo, query.getKeyword()));
        }

        // 只查询已完成的档案
        wrapper.eq(PDITask::getTaskStatus, PDITaskStatusEnum.COMPLETED.getCode());

        wrapper.orderByDesc(PDITask::getStartTime);

        Page<PDITask> pageResult = page(pageParam, wrapper);

        List<ArchiveVO> list = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    public ArchiveDetailVO getArchiveDetail(Long archiveId) {
        PDITask task = getById(archiveId);
        if (task == null) {
            throw new BusinessException(3001, "档案不存在");
        }

        ArchiveDetailVO vo = convertToDetailVO(task);

        // 查询关联报警
        List<Alarm> alarms = alarmMapper.selectList(
                new LambdaQueryWrapper<Alarm>()
                        .eq(Alarm::getPdiTaskId, archiveId)
                        .orderByDesc(Alarm::getAlarmTime));

        if (!CollectionUtils.isEmpty(alarms)) {
            List<ArchiveDetailVO.ArchiveAlarmVO> alarmVOs = alarms.stream().map(alarm -> {
                ArchiveDetailVO.ArchiveAlarmVO alarmVO = new ArchiveDetailVO.ArchiveAlarmVO();
                alarmVO.setId(alarm.getId());
                alarmVO.setAlarmType(alarm.getAlarmType());
                // TODO: 设置报警类型名称
                alarmVO.setAlarmTypeName("报警类型" + alarm.getAlarmType());
                alarmVO.setAlarmTime(alarm.getAlarmTime());
                return alarmVO;
            }).collect(Collectors.toList());
            vo.setAlarms(alarmVOs);
        }

        return vo;
    }

    @Override
    public List<TimelineEventVO> getTimeline(Long archiveId) {
        PDITask task = getById(archiveId);
        if (task == null) {
            throw new BusinessException(3001, "档案不存在");
        }

        List<TimelineEvent> events = timelineEventMapper.selectList(
                new LambdaQueryWrapper<TimelineEvent>()
                        .eq(TimelineEvent::getTaskId, archiveId)
                        .orderByAsc(TimelineEvent::getEventTime));

        return events.stream().map(event -> {
            TimelineEventVO vo = new TimelineEventVO();
            BeanUtils.copyProperties(event, vo);
            vo.setId(event.getId());
            vo.setEventType(String.valueOf(event.getEventType()));
            vo.setEventTypeName(getEventTypeName(event.getEventType()));
            vo.setDescription(event.getEventDesc());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String generateImagePackage(Long archiveId) {
        PDITask task = getById(archiveId);
        if (task == null) {
            throw new BusinessException(3001, "档案不存在");
        }

        // TODO: 实现图片包生成逻辑
        // 1. 获取所有相关图片
        // 2. 创建ZIP文件
        // 3. 上传到MinIO
        // 4. 生成临时下载链接

        log.info("生成图片包: archiveId={}", archiveId);

        return "https://minio.example.com/packages/archive_" + archiveId + ".zip?token=xxx";
    }

    @Override
    public ArchiveVO getInProgressArchive(Long channelId) {
        PDITask task = lambdaQuery()
                .eq(PDITask::getChannelId, channelId)
                .eq(PDITask::getTaskStatus, PDITaskStatusEnum.IN_PROGRESS.getCode())
                .one();

        return task != null ? convertToVO(task) : null;
    }

    // ==================== 私有方法 ====================

    private String generateTaskNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%04d", (int) (Math.random() * 10000));
        return TASK_NO_PREFIX + dateStr + seq;
    }

    private Integer determineTaskResult(long durationMinutes, ArchiveCompleteDTO dto) {
        // 如果有时长异常，判定为异常
        if (durationMinutes < 5 || durationMinutes > 120) {
            return PDITaskResultEnum.ABNORMAL.getCode();
        }

        // 如果有抽烟行为，判定为异常
        if (Boolean.TRUE.equals(dto.getHasSmoking())) {
            return PDITaskResultEnum.ABNORMAL.getCode();
        }

        // 时长超过标准，判定为超时
        if (durationMinutes > STANDARD_DURATION) {
            return PDITaskResultEnum.TIMEOUT.getCode();
        }

        // 正常完成
        return PDITaskResultEnum.QUALIFIED.getCode();
    }

    private ArchiveVO convertToVO(PDITask task) {
        ArchiveVO vo = new ArchiveVO();
        BeanUtils.copyProperties(task, vo);
        vo.setId(task.getId());

        // 设置作业状态名称
        PDITaskStatusEnum statusEnum = PDITaskStatusEnum.getByCode(task.getTaskStatus());
        if (statusEnum != null) {
            vo.setTaskStatusName(statusEnum.getName());
        }

        // 设置作业结果名称
        if (task.getTaskResult() != null) {
            PDITaskResultEnum resultEnum = PDITaskResultEnum.getByCode(task.getTaskResult());
            if (resultEnum != null) {
                vo.setTaskResultName(resultEnum.getName());
            }
        }

        // 转换时长为分钟
        if (task.getDurationSeconds() != null) {
            vo.setDuration(task.getDurationSeconds() / 60L);
        }
        vo.setStandardDuration(task.getStandardDuration() / 60);

        // 查询站点名称
        if (task.getSiteId() != null) {
            Site site = siteMapper.selectById(task.getSiteId());
            if (site != null) {
                vo.setSiteName(site.getSiteName());
            }
        }

        // 查询通道名称
        if (task.getChannelId() != null) {
            Channel channel = channelMapper.selectById(task.getChannelId());
            if (channel != null) {
                vo.setChannelName(channel.getChannelName());
            }
        }

        // 查询报警数量
        Long alarmCount = alarmMapper.selectCount(
                new LambdaQueryWrapper<Alarm>().eq(Alarm::getPdiTaskId, task.getId()));
        vo.setAlarmCount(alarmCount.intValue());

        return vo;
    }

    private ArchiveDetailVO convertToDetailVO(PDITask task) {
        ArchiveDetailVO vo = new ArchiveDetailVO();
        BeanUtils.copyProperties(convertToVO(task), vo);
        vo.setEnterStateSeq(task.getEnterStateSeq());
        vo.setExitStateSeq(task.getExitStateSeq());
        return vo;
    }

    private String getEventTypeName(Integer eventType) {
        return switch (eventType) {
            case 1 -> "进入";
            case 2 -> "离开";
            case 3 -> "报警";
            case 4 -> "状态变更";
            default -> "其他";
        };
    }

}
