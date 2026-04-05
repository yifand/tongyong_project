package com.vdc.pdi.behaviorarchive.service;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.dto.request.ArchiveListRequest;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveResponse;
import com.vdc.pdi.behaviorarchive.enums.ArchiveStatus;
import com.vdc.pdi.common.dto.PageResult;

/**
 * 行为档案服务接口
 */
public interface BehaviorArchiveService {

    /**
     * 查询档案列表
     *
     * @param request 查询条件
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<ArchiveResponse> queryArchiveList(ArchiveListRequest request, int page, int size);

    /**
     * 获取档案详情
     *
     * @param archiveId 档案ID
     * @return 档案详情
     */
    ArchiveDetailResponse getArchiveDetail(Long archiveId);

    /**
     * 计算档案状态
     *
     * @param archive 档案实体
     * @return 档案状态
     */
    ArchiveStatus calculateArchiveStatus(BehaviorArchive archive);

    /**
     * 创建或更新档案
     *
     * @param archive 档案实体
     * @return 保存后的档案
     */
    BehaviorArchive createOrUpdateArchive(BehaviorArchive archive);

    /**
     * 检查用户是否有权限访问档案
     *
     * @param archive 档案实体
     * @return true-有权限，false-无权限
     */
    boolean hasPermission(BehaviorArchive archive);
}
