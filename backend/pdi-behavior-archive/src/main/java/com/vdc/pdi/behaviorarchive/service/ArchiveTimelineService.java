package com.vdc.pdi.behaviorarchive.service;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.domain.entity.PdiTask;
import com.vdc.pdi.behaviorarchive.dto.response.TimelineItemDTO;

import java.util.List;

/**
 * 档案时间线服务接口
 */
public interface ArchiveTimelineService {

    /**
     * 组装时间线
     *
     * @param archive 档案实体
     * @param pdiTask PDI任务
     * @return 时间线节点列表
     */
    List<TimelineItemDTO> assembleTimeline(BehaviorArchive archive, PdiTask pdiTask);

    /**
     * 根据档案ID获取时间线
     *
     * @param archiveId 档案ID
     * @return 时间线节点列表
     */
    List<TimelineItemDTO> getTimelineByArchiveId(Long archiveId);
}
