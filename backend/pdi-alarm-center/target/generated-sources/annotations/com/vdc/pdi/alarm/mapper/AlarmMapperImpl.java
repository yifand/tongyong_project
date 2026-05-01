package com.vdc.pdi.alarm.mapper;

import com.vdc.pdi.alarm.domain.entity.AlarmRecord;
import com.vdc.pdi.alarm.domain.vo.AlarmStatisticsVO;
import com.vdc.pdi.alarm.dto.response.AlarmResponse;
import com.vdc.pdi.alarm.dto.response.AlarmStatisticsResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-11T11:59:41+0800",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AlarmMapperImpl implements AlarmMapper {

    @Override
    public AlarmResponse toResponse(AlarmRecord alarm) {
        if ( alarm == null ) {
            return null;
        }

        AlarmResponse alarmResponse = new AlarmResponse();

        alarmResponse.setTypeName( mapTypeName( alarm.getType() ) );
        alarmResponse.setStatusName( mapStatusName( alarm.getStatus() ) );
        alarmResponse.setExtraInfo( mapExtraInfo( alarm.getExtraInfo() ) );
        alarmResponse.setAlarmTime( alarm.getAlarmTime() );
        alarmResponse.setChannelId( alarm.getChannelId() );
        alarmResponse.setCreatedAt( alarm.getCreatedAt() );
        alarmResponse.setFaceImageUrl( alarm.getFaceImageUrl() );
        alarmResponse.setId( alarm.getId() );
        alarmResponse.setLocation( alarm.getLocation() );
        alarmResponse.setProcessedAt( alarm.getProcessedAt() );
        alarmResponse.setProcessorId( alarm.getProcessorId() );
        alarmResponse.setRemark( alarm.getRemark() );
        alarmResponse.setSceneImageUrl( alarm.getSceneImageUrl() );
        alarmResponse.setSiteId( alarm.getSiteId() );
        alarmResponse.setStatus( alarm.getStatus() );
        alarmResponse.setType( alarm.getType() );

        return alarmResponse;
    }

    @Override
    public List<AlarmResponse> toResponseList(List<AlarmRecord> alarms) {
        if ( alarms == null ) {
            return null;
        }

        List<AlarmResponse> list = new ArrayList<AlarmResponse>( alarms.size() );
        for ( AlarmRecord alarmRecord : alarms ) {
            list.add( toResponse( alarmRecord ) );
        }

        return list;
    }

    @Override
    public AlarmStatisticsResponse toStatisticsResponse(AlarmStatisticsVO statistics) {
        if ( statistics == null ) {
            return null;
        }

        AlarmStatisticsResponse alarmStatisticsResponse = new AlarmStatisticsResponse();

        alarmStatisticsResponse.setProcessRate( calculateProcessRate( statistics ) );
        alarmStatisticsResponse.setProcessed( statistics.getProcessed() );
        alarmStatisticsResponse.setTotal( statistics.getTotal() );
        alarmStatisticsResponse.setUnprocessed( statistics.getUnprocessed() );

        return alarmStatisticsResponse;
    }
}
