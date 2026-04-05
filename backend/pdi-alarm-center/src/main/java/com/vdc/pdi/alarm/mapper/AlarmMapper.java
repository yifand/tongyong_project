package com.vdc.pdi.alarm.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.domain.vo.AlarmStatisticsVO;
import com.vdc.pdi.alarm.dto.response.AlarmExtraInfo;
import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.dto.response.AlarmStatisticsResponse;
import com.vdc.pdi.common.enums.AlarmStatusEnum;
import com.vdc.pdi.common.enums.AlarmTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 报警对象映射器
 */
@Mapper(componentModel = "spring")
public interface AlarmMapper {

    /**
     * 转换为响应对象
     */
    @Mapping(target = "typeName", source = "type", qualifiedByName = "mapTypeName")
    @Mapping(target = "statusName", source = "status", qualifiedByName = "mapStatusName")
    @Mapping(target = "extraInfo", source = "extraInfo", qualifiedByName = "mapExtraInfo")
    @Mapping(target = "siteName", ignore = true)
    @Mapping(target = "channelName", ignore = true)
    @Mapping(target = "processorName", ignore = true)
    AlarmResponse toResponse(AlarmRecord alarm);

    /**
     * 转换为响应列表
     */
    List<AlarmResponse> toResponseList(List<AlarmRecord> alarms);

    /**
     * 统计VO转换为响应
     */
    @Mapping(target = "processRate", source = ".", qualifiedByName = "calculateProcessRate")
    AlarmStatisticsResponse toStatisticsResponse(AlarmStatisticsVO statistics);

    @Named("mapTypeName")
    default String mapTypeName(Integer type) {
        if (type == null) {
            return null;
        }
        try {
            return AlarmTypeEnum.fromCode(type).getName();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    @Named("mapStatusName")
    default String mapStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        try {
            return AlarmStatusEnum.fromCode(status).getName();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    @Named("calculateProcessRate")
    default String calculateProcessRate(AlarmStatisticsVO statistics) {
        if (statistics.getTotal() == null || statistics.getTotal() == 0) {
            return "0.0%";
        }
        long processed = statistics.getProcessed() != null ? statistics.getProcessed() : 0;
        long total = statistics.getTotal();
        double rate = (double) processed / total * 100;
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(rate) + "%";
    }

    @Named("mapExtraInfo")
    default AlarmExtraInfo mapExtraInfo(String extraInfo) {
        if (extraInfo == null || extraInfo.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(extraInfo, AlarmExtraInfo.class);
        } catch (JsonProcessingException e) {
            // 解析失败时返回null
            return null;
        }
    }
}
