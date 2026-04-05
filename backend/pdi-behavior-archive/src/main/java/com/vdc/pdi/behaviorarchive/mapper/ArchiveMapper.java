package com.vdc.pdi.behaviorarchive.mapper;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveResponse;
import com.vdc.pdi.behaviorarchive.enums.ArchiveStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 档案对象映射器
 */
@Mapper(componentModel = "spring")
public interface ArchiveMapper {

    /**
     * 转换为列表响应
     */
    @Mapping(target = "statusText", source = "status", qualifiedByName = "statusToText")
    ArchiveResponse toResponse(BehaviorArchive archive);

    /**
     * 转换为列表响应列表
     */
    List<ArchiveResponse> toResponseList(List<BehaviorArchive> archives);

    /**
     * 转换为详情响应
     */
    @Mapping(target = "statusText", source = "status", qualifiedByName = "statusToText")
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "timeline", ignore = true)
    ArchiveDetailResponse toDetailResponse(BehaviorArchive archive);

    /**
     * 状态码转文本
     */
    @Named("statusToText")
    default String statusToText(Integer status) {
        return ArchiveStatus.fromCode(status).getText();
    }
}
