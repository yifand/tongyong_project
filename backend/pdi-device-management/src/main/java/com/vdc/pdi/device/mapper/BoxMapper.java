package com.vdc.pdi.device.mapper;

import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.dto.request.BoxRequest;
import com.vdc.pdi.device.dto.response.BoxResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 盒子映射器
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BoxMapper {

    /**
     * 请求转换为实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastHeartbeatAt", ignore = true)
    @Mapping(target = "cpuUsage", ignore = true)
    @Mapping(target = "memoryUsage", ignore = true)
    @Mapping(target = "diskUsage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    EdgeBox toEntity(BoxRequest request);

    /**
     * 更新实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastHeartbeatAt", ignore = true)
    @Mapping(target = "cpuUsage", ignore = true)
    @Mapping(target = "memoryUsage", ignore = true)
    @Mapping(target = "diskUsage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(BoxRequest request, @MappingTarget EdgeBox entity);

    /**
     * 实体转换为响应
     */
    @Mapping(target = "siteName", ignore = true)
    @Mapping(target = "statusText", expression = "java(entity.getStatusText())")
    BoxResponse toResponse(EdgeBox entity);

    /**
     * 批量转换
     */
    List<BoxResponse> toResponseList(List<EdgeBox> entities);
}
