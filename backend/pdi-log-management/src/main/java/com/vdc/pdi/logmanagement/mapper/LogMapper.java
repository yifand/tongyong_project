package com.vdc.pdi.logmanagement.mapper;

import com.vdc.pdi.logmanagement.domain.entity.OperationLog;
import com.vdc.pdi.logmanagement.domain.entity.SystemLog;
import com.vdc.pdi.logmanagement.dto.response.OperationLogResponse;
import com.vdc.pdi.logmanagement.dto.response.SystemLogResponse;
import com.vdc.pdi.logmanagement.enums.LogLevel;
import com.vdc.pdi.logmanagement.enums.OperationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 日志映射器
 */
@Mapper(componentModel = "spring")
public interface LogMapper {

    @Mapping(source = "operationType", target = "operationTypeCode")
    @Mapping(source = "operationType", target = "operationType", qualifiedByName = "mapOperationType")
    OperationLogResponse toOperationLogResponse(OperationLog operationLog);

    List<OperationLogResponse> toOperationLogResponseList(List<OperationLog> operationLogs);

    @Mapping(source = "level", target = "levelCode")
    @Mapping(source = "level", target = "level", qualifiedByName = "mapLogLevel")
    SystemLogResponse toSystemLogResponse(SystemLog systemLog);

    List<SystemLogResponse> toSystemLogResponseList(List<SystemLog> systemLogs);

    @Named("mapOperationType")
    default String mapOperationType(Integer code) {
        return OperationType.fromCode(code).getName();
    }

    @Named("mapLogLevel")
    default String mapLogLevel(Integer code) {
        return LogLevel.fromCode(code).getName();
    }
}
