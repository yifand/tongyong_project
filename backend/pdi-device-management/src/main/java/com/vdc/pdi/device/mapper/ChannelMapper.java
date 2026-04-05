package com.vdc.pdi.device.mapper;

import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.dto.request.ChannelRequest;
import com.vdc.pdi.device.dto.response.ChannelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import org.mapstruct.Named;

import java.util.List;

/**
 * 通道映射器
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChannelMapper {

    /**
     * 请求转换为实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Channel toEntity(ChannelRequest request);

    /**
     * 更新实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(ChannelRequest request, @MappingTarget Channel entity);

    /**
     * 实体转换为响应
     */
    @Mapping(target = "boxName", ignore = true)
    @Mapping(target = "siteName", ignore = true)
    @Mapping(target = "typeText", expression = "java(entity.getTypeText())")
    @Mapping(target = "statusText", expression = "java(entity.getStatusText())")
    @Mapping(target = "algorithmTypeText", expression = "java(getAlgorithmTypeText(entity.getAlgorithmType()))")
    @Mapping(target = "rtspUrl", expression = "java(maskRtspUrl(entity.getRtspUrl()))")
    ChannelResponse toResponse(Channel entity);

    /**
     * 批量转换
     */
    List<ChannelResponse> toResponseList(List<Channel> entities);

    /**
     * 获取算法类型文本
     */
    @Named("getAlgorithmTypeText")
    default String getAlgorithmTypeText(String algorithmType) {
        if (algorithmType == null) {
            return null;
        }
        return switch (algorithmType) {
            case "smoke" -> "抽烟检测";
            case "pdi_left_front" -> "PDI左前门检测";
            case "pdi_left_rear" -> "PDI左后门检测";
            case "pdi_slide" -> "PDI滑移门检测";
            default -> algorithmType;
        };
    }

    /**
     * RTSP地址脱敏
     */
    @Named("maskRtspUrl")
    default String maskRtspUrl(String rtspUrl) {
        if (rtspUrl == null || rtspUrl.isEmpty()) {
            return rtspUrl;
        }
        // 简单的脱敏处理，将IP地址部分替换为***
        return rtspUrl.replaceAll("(rtsp://)([^:]+):([^@]+)@", "$1***:***@")
                      .replaceAll("(rtsp://[\\d.]+)", "rtsp://***.***.***.***");
    }
}
