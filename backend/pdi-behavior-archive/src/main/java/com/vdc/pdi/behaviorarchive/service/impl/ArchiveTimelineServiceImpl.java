package com.vdc.pdi.behaviorarchive.service.impl;

import com.vdc.pdi.behaviorarchive.domain.entity.ArchiveTimeline;
import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.entity.PdiTask;
import com.vdc.pdi.behaviorarchive.domain.entity.StateStream;
import com.vdc.pdi.behaviorarchive.domain.repository.ArchiveTimelineRepository;
import com.vdc.pdi.behaviorarchive.domain.repository.StateStreamRepository;
import com.vdc.pdi.behaviorarchive.dto.response.TimelineItemDTO;
import com.vdc.pdi.behaviorarchive.service.ArchiveTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 档案时间线服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArchiveTimelineServiceImpl implements ArchiveTimelineService {

    private final ArchiveTimelineRepository timelineRepository;
    private final StateStreamRepository stateStreamRepository;

    @Override
    public List<TimelineItemDTO> assembleTimeline(BehaviorArchive archive, PdiTask pdiTask) {
        List<TimelineItemDTO> timeline = new ArrayList<>();

        LocalDateTime startTime = pdiTask.getStartTime();
        if (startTime == null) {
            log.warn("PDI任务开始时间为空, taskId={}", pdiTask.getId());
            return timeline;
        }

        // 1. 添加起始节点
        timeline.add(TimelineItemDTO.builder()
                .seq(1)
                .eventTime(startTime)
                .action("人员进入车内")
                .imageUrl(getEnterImageUrl(pdiTask))
                .nodeType("start")
                .offsetSeconds(0)
                .build());

        // 2. 查询并添加中间节点
        List<ArchiveTimeline> middleNodes = timelineRepository
                .findByArchiveIdAndSeqGreaterThanOrderBySeqAsc(archive.getId(), 1);

        int seq = 2;
        for (ArchiveTimeline node : middleNodes) {
            // 跳过结束节点（如果PDI任务已完成且该节点是结束时间）
            if (pdiTask.getEndTime() != null &&
                    node.getEventTime().equals(pdiTask.getEndTime())) {
                continue;
            }

            timeline.add(TimelineItemDTO.builder()
                    .seq(seq++)
                    .eventTime(node.getEventTime())
                    .action(node.getAction())
                    .imageUrl(node.getImageUrl())
                    .nodeType("process")
                    .offsetSeconds((int) Duration.between(startTime, node.getEventTime()).getSeconds())
                    .build());
        }

        // 3. 添加结束节点（如果作业已完成）
        if (pdiTask.getEndTime() != null) {
            timeline.add(TimelineItemDTO.builder()
                    .seq(seq)
                    .eventTime(pdiTask.getEndTime())
                    .action("人员离开，检查结束")
                    .imageUrl(getExitImageUrl(pdiTask))
                    .nodeType("end")
                    .offsetSeconds((int) Duration.between(startTime, pdiTask.getEndTime()).getSeconds())
                    .build());
        }

        return timeline;
    }

    @Override
    public List<TimelineItemDTO> getTimelineByArchiveId(Long archiveId) {
        List<ArchiveTimeline> timelines = timelineRepository.findByArchiveIdOrderBySeqAsc(archiveId);
        List<TimelineItemDTO> result = new ArrayList<>();

        for (ArchiveTimeline timeline : timelines) {
            result.add(TimelineItemDTO.builder()
                    .seq(timeline.getSeq())
                    .eventTime(timeline.getEventTime())
                    .action(timeline.getAction())
                    .imageUrl(timeline.getImageUrl())
                    .nodeType("process")
                    .offsetSeconds(0)
                    .build());
        }

        return result;
    }

    /**
     * 获取人员进入时的截图URL
     */
    private String getEnterImageUrl(PdiTask pdiTask) {
        try {
            // 从state_stream获取人员进入时的截图（状态码3表示S3：门开+有人+未进出）
            Optional<StateStream> enterState = stateStreamRepository
                    .findFirstByChannelIdAndStateCodeAndEventTimeGreaterThanEqualOrderByEventTimeAsc(
                            pdiTask.getChannelId(), 3, pdiTask.getStartTime());
            return enterState.map(StateStream::getImageUrl).orElse(null);
        } catch (Exception e) {
            log.warn("获取进入截图失败, taskId={}", pdiTask.getId(), e);
            return null;
        }
    }

    /**
     * 获取人员离开时的截图URL
     */
    private String getExitImageUrl(PdiTask pdiTask) {
        try {
            if (pdiTask.getEndTime() == null) {
                return null;
            }
            // 从state_stream获取人员离开时的截图（状态码1或5表示S1/S5：门关+无人/门开+无人）
            List<Integer> exitStateCodes = List.of(1, 5);
            Optional<StateStream> exitState = stateStreamRepository
                    .findFirstByChannelIdAndStateCodeInAndEventTimeLessThanEqualOrderByEventTimeDesc(
                            pdiTask.getChannelId(), exitStateCodes, pdiTask.getEndTime());
            return exitState.map(StateStream::getImageUrl).orElse(null);
        } catch (Exception e) {
            log.warn("获取离开截图失败, taskId={}", pdiTask.getId(), e);
            return null;
        }
    }
}
