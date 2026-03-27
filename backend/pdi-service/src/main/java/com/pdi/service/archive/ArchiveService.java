package com.pdi.service.archive;

import com.pdi.common.result.PageResult;
import com.pdi.service.archive.dto.ArchiveCompleteDTO;
import com.pdi.service.archive.dto.ArchiveQueryDTO;
import com.pdi.service.archive.vo.ArchiveDetailVO;
import com.pdi.service.archive.vo.ArchiveVO;
import com.pdi.service.archive.vo.TimelineEventVO;

import java.util.List;

/**
 * 档案服务接口
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface ArchiveService {

    /**
     * 创建PDI作业档案
     *
     * @param channelId 通道ID
     * @return 创建的档案信息
     */
    ArchiveVO createArchive(Long channelId);

    /**
     * 完成PDI作业档案
     *
     * @param archiveId 档案ID
     * @param dto       完成信息
     */
    void completeArchive(Long archiveId, ArchiveCompleteDTO dto);

    /**
     * 获取PDI作业档案列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<ArchiveVO> listArchives(ArchiveQueryDTO query);

    /**
     * 获取档案详情
     *
     * @param archiveId 档案ID
     * @return 档案详情
     */
    ArchiveDetailVO getArchiveDetail(Long archiveId);

    /**
     * 获取时间线
     *
     * @param archiveId 档案ID
     * @return 时间线事件列表
     */
    List<TimelineEventVO> getTimeline(Long archiveId);

    /**
     * 生成图片包下载链接
     *
     * @param archiveId 档案ID
     * @return 下载链接
     */
    String generateImagePackage(Long archiveId);

    /**
     * 获取进行中的档案
     *
     * @param channelId 通道ID
     * @return 档案信息
     */
    ArchiveVO getInProgressArchive(Long channelId);

}
