package com.vdc.pdi.device.mapper;

import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.dto.request.BoxRequest;
import com.vdc.pdi.device.dto.response.BoxResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-06T00:52:00+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class BoxMapperImpl implements BoxMapper {

    @Override
    public EdgeBox toEntity(BoxRequest request) {
        if ( request == null ) {
            return null;
        }

        EdgeBox edgeBox = new EdgeBox();

        edgeBox.setSiteId( request.getSiteId() );
        edgeBox.setName( request.getName() );
        edgeBox.setIpAddress( request.getIpAddress() );
        edgeBox.setVersion( request.getVersion() );

        return edgeBox;
    }

    @Override
    public void updateEntity(BoxRequest request, EdgeBox entity) {
        if ( request == null ) {
            return;
        }

        if ( request.getSiteId() != null ) {
            entity.setSiteId( request.getSiteId() );
        }
        if ( request.getName() != null ) {
            entity.setName( request.getName() );
        }
        if ( request.getIpAddress() != null ) {
            entity.setIpAddress( request.getIpAddress() );
        }
        if ( request.getVersion() != null ) {
            entity.setVersion( request.getVersion() );
        }
    }

    @Override
    public BoxResponse toResponse(EdgeBox entity) {
        if ( entity == null ) {
            return null;
        }

        BoxResponse boxResponse = new BoxResponse();

        boxResponse.setId( entity.getId() );
        boxResponse.setName( entity.getName() );
        boxResponse.setSiteId( entity.getSiteId() );
        boxResponse.setIpAddress( entity.getIpAddress() );
        boxResponse.setStatus( entity.getStatus() );
        boxResponse.setLastHeartbeatAt( entity.getLastHeartbeatAt() );
        boxResponse.setVersion( entity.getVersion() );
        boxResponse.setCpuUsage( entity.getCpuUsage() );
        boxResponse.setMemoryUsage( entity.getMemoryUsage() );
        boxResponse.setDiskUsage( entity.getDiskUsage() );
        boxResponse.setCreatedAt( entity.getCreatedAt() );
        boxResponse.setUpdatedAt( entity.getUpdatedAt() );

        boxResponse.setStatusText( entity.getStatusText() );

        return boxResponse;
    }

    @Override
    public List<BoxResponse> toResponseList(List<EdgeBox> entities) {
        if ( entities == null ) {
            return null;
        }

        List<BoxResponse> list = new ArrayList<BoxResponse>( entities.size() );
        for ( EdgeBox edgeBox : entities ) {
            list.add( toResponse( edgeBox ) );
        }

        return list;
    }
}
