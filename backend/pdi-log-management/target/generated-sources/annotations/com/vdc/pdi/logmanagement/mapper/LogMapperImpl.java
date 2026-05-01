package com.vdc.pdi.logmanagement.mapper;

import com.vdc.pdi.logmanagement.domain.entity.OperationLog;
import com.vdc.pdi.logmanagement.domain.entity.SystemLog;
import com.vdc.pdi.logmanagement.dto.response.OperationLogResponse;
import com.vdc.pdi.logmanagement.dto.response.SystemLogResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-11T11:59:43+0800",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class LogMapperImpl implements LogMapper {

    @Override
    public OperationLogResponse toOperationLogResponse(OperationLog operationLog) {
        if ( operationLog == null ) {
            return null;
        }

        OperationLogResponse.OperationLogResponseBuilder operationLogResponse = OperationLogResponse.builder();

        operationLogResponse.operationTypeCode( operationLog.getOperationType() );
        operationLogResponse.operationType( mapOperationType( operationLog.getOperationType() ) );
        operationLogResponse.createdAt( operationLog.getCreatedAt() );
        operationLogResponse.errorMsg( operationLog.getErrorMsg() );
        operationLogResponse.executionTime( operationLog.getExecutionTime() );
        operationLogResponse.id( operationLog.getId() );
        operationLogResponse.ipAddress( operationLog.getIpAddress() );
        operationLogResponse.operationDetail( operationLog.getOperationDetail() );
        operationLogResponse.requestParams( operationLog.getRequestParams() );
        operationLogResponse.result( operationLog.getResult() );
        operationLogResponse.userId( operationLog.getUserId() );
        operationLogResponse.username( operationLog.getUsername() );

        return operationLogResponse.build();
    }

    @Override
    public List<OperationLogResponse> toOperationLogResponseList(List<OperationLog> operationLogs) {
        if ( operationLogs == null ) {
            return null;
        }

        List<OperationLogResponse> list = new ArrayList<OperationLogResponse>( operationLogs.size() );
        for ( OperationLog operationLog : operationLogs ) {
            list.add( toOperationLogResponse( operationLog ) );
        }

        return list;
    }

    @Override
    public SystemLogResponse toSystemLogResponse(SystemLog systemLog) {
        if ( systemLog == null ) {
            return null;
        }

        SystemLogResponse.SystemLogResponseBuilder systemLogResponse = SystemLogResponse.builder();

        systemLogResponse.levelCode( systemLog.getLevel() );
        systemLogResponse.level( mapLogLevel( systemLog.getLevel() ) );
        systemLogResponse.createdAt( systemLog.getCreatedAt() );
        systemLogResponse.id( systemLog.getId() );
        systemLogResponse.message( systemLog.getMessage() );
        systemLogResponse.module( systemLog.getModule() );
        systemLogResponse.sourceClass( systemLog.getSourceClass() );
        systemLogResponse.sourceMethod( systemLog.getSourceMethod() );
        systemLogResponse.stackTrace( systemLog.getStackTrace() );
        systemLogResponse.threadName( systemLog.getThreadName() );

        return systemLogResponse.build();
    }

    @Override
    public List<SystemLogResponse> toSystemLogResponseList(List<SystemLog> systemLogs) {
        if ( systemLogs == null ) {
            return null;
        }

        List<SystemLogResponse> list = new ArrayList<SystemLogResponse>( systemLogs.size() );
        for ( SystemLog systemLog : systemLogs ) {
            list.add( toSystemLogResponse( systemLog ) );
        }

        return list;
    }
}
