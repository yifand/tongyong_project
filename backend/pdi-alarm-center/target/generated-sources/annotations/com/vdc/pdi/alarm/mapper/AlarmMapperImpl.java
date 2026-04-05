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
    date = "2026-04-06T00:55:43+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
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
        alarmResponse.setId( alarm.getId() );
        alarmResponse.setType( alarm.getType() );
        alarmResponse.setSiteId( alarm.getSiteId() );
        alarmResponse.setChannelId( alarm.getChannelId() );
        alarmResponse.setAlarmTime( alarm.getAlarmTime() );
        alarmResponse.setLocation( alarm.getLocation() );
        alarmResponse.setFaceImageUrl( alarm.getFaceImageUrl() );
        alarmResponse.setSceneImageUrl( alarm.getSceneImageUrl() );
        alarmResponse.setStatus( alarm.getStatus() );
        alarmResponse.setProcessorId( alarm.getProcessorId() );
        alarmResponse.setProcessedAt( alarm.getProcessedAt() );
        alarmResponse.setRemark( alarm.getRemark() );
        alarmResponse.setCreatedAt( alarm.getCreatedAt() );

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
        alarmStatisticsResponse.setTotal( statistics.getTotal() );
        alarmStatisticsResponse.setUnprocessed( statistics.getUnprocessed() );
        alarmStatisticsResponse.setProcessed( statistics.getProcessed() );

        return alarmStatisticsResponse;
    }
}
