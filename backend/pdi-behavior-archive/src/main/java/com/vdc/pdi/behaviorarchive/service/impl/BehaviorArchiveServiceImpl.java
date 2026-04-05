package com.vdc.pdi.behaviorarchive.service.impl;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.entity.PdiTask;
import com.vdc.pdi.behaviorarchive.domain.repository.BehaviorArchiveRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.PdiTaskRepository;
import com.vdc.pdi.behaviorarchive.dto.request.ArchiveListRequest;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveResponse;
import com.vdc.pdi.behaviorarchive.enums.ArchiveStatus;
import com.vdc.pdi.behaviorarchive.exception.ArchiveException;
import com.vdc.pdi.behaviorarchive.mapper.ArchiveMapper;
import com.vdc.pdi.behaviorarchive.service.ArchiveTimelineService;
import com.vdc.pdi.behaviorarchive.service.BehaviorArchiveService;
import com.vdc.pdi.common.dto.PageResult;
import com.vdc.pdi.common.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 行为档案服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BehaviorArchiveServiceImpl implements BehaviorArchiveService {

    private final BehaviorArchiveRepository archiveRepository;
    private final PdiTaskRepository pdiTaskRepository;
    private final ArchiveTimelineService timelineService;
    private final ArchiveMapper archiveMapper;

    @Override
    public PageResult<ArchiveResponse> queryArchiveList(ArchiveListRequest request, int page, int size) {
        // 构建分页对象
        Pageable pageable = PageRequest.of(page - 1, size);

        // 执行查询
        Page<BehaviorArchive> archivePage = archiveRepository.findArchivesWithFilters(
                request.getSiteId(),
                request.getStatus(),
                request.getChannelId(),
                request.getStartTimeFrom(),
                request.getStartTimeTo(),
                pageable
        );

        // 转换为响应对象
        return new PageResult<>(
                archiveMapper.toResponseList(archivePage.getContent()),
                archivePage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public ArchiveDetailResponse getArchiveDetail(Long archiveId) {
        // 查询档案
        BehaviorArchive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new ArchiveException("档案不存在"));

        // 权限校验
        if (!hasPermission(archive)) {
            throw new ArchiveException(ResultCode.PERMISSION_DENIED, "无权访问该档案");
        }

        // 查询关联的PDI任务
        PdiTask pdiTask = pdiTaskRepository.findById(archive.getPdiTaskId())
                .orElseThrow(() -> new ArchiveException(ResultCode.DATA_NOT_FOUND, "关联的PDI任务不存在"));

        // 转换为详情响应
        ArchiveDetailResponse detailResponse = archiveMapper.toDetailResponse(archive);

        // 组装时间线
        detailResponse.setTimeline(timelineService.assembleTimeline(archive, pdiTask));

        return detailResponse;
    }

    @Override
    public ArchiveStatus calculateArchiveStatus(BehaviorArchive archive) {
        // 进行中状态
        if (archive.getEndTime() == null) {
            return ArchiveStatus.IN_PROGRESS;
        }

        // 没有预估时长，无法判断达标情况
        if (archive.getEstimatedDuration() == null || archive.getEstimatedDuration() <= 0) {
            return ArchiveStatus.UNQUALIFIED;
        }

        // 没有实际时长，无法判断达标情况
        if (archive.getActualDuration() == null) {
            return ArchiveStatus.UNQUALIFIED;
        }

        // 计算标准时长的90%（转换为秒）
        int standardSeconds = archive.getEstimatedDuration() * 60;
        int threshold = (int) (standardSeconds * 0.9);

        // 判断达标情况
        if (archive.getActualDuration() >= threshold) {
            return ArchiveStatus.QUALIFIED;
        } else {
            return ArchiveStatus.UNQUALIFIED;
        }
    }

    @Override
    @Transactional
    public BehaviorArchive createOrUpdateArchive(BehaviorArchive archive) {
        if (archive.getId() == null) {
            // 创建新档案
            log.info("创建新档案, pdiTaskId={}", archive.getPdiTaskId());
            archive.setStatus(ArchiveStatus.IN_PROGRESS.getCode());
        } else {
            // 更新已有档案
            log.info("更新档案, id={}", archive.getId());
            // 重新计算状态
            ArchiveStatus status = calculateArchiveStatus(archive);
            archive.setStatus(status.getCode());
        }
        return archiveRepository.save(archive);
    }

    @Override
    public boolean hasPermission(BehaviorArchive archive) {
        // TODO: 从SecurityContext获取当前用户站点ID进行权限校验
        // 暂时返回true，实际项目中需要根据用户权限判断
        return true;
    }
}
