package com.vdc.pdi.device.mapper;

import com.vdc.pdi.device.domain.entity.Channel;
import com.vdc.pdi.device.dto.request.ChannelRequest;
import com.vdc.pdi.device.dto.response.ChannelResponse;
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
public class ChannelMapperImpl implements ChannelMapper {

    @Override
    public Channel toEntity(ChannelRequest request) {
        if ( request == null ) {
            return null;
        }

        Channel channel = new Channel();

        channel.setSiteId( request.getSiteId() );
        channel.setBoxId( request.getBoxId() );
        channel.setName( request.getName() );
        channel.setType( request.getType() );
        channel.setAlgorithmType( request.getAlgorithmType() );
        channel.setRtspUrl( request.getRtspUrl() );

        return channel;
    }

    @Override
    public void updateEntity(ChannelRequest request, Channel entity) {
        if ( request == null ) {
            return;
        }

        if ( request.getSiteId() != null ) {
            entity.setSiteId( request.getSiteId() );
        }
        if ( request.getBoxId() != null ) {
            entity.setBoxId( request.getBoxId() );
        }
        if ( request.getName() != null ) {
            entity.setName( request.getName() );
        }
        if ( request.getType() != null ) {
            entity.setType( request.getType() );
        }
        if ( request.getAlgorithmType() != null ) {
            entity.setAlgorithmType( request.getAlgorithmType() );
        }
        if ( request.getRtspUrl() != null ) {
            entity.setRtspUrl( request.getRtspUrl() );
        }
    }

    @Override
    public ChannelResponse toResponse(Channel entity) {
        if ( entity == null ) {
            return null;
        }

        ChannelResponse channelResponse = new ChannelResponse();

        channelResponse.setId( entity.getId() );
        channelResponse.setName( entity.getName() );
        channelResponse.setBoxId( entity.getBoxId() );
        channelResponse.setSiteId( entity.getSiteId() );
        channelResponse.setType( entity.getType() );
        channelResponse.setStatus( entity.getStatus() );
        channelResponse.setAlgorithmType( entity.getAlgorithmType() );
        channelResponse.setCreatedAt( entity.getCreatedAt() );
        channelResponse.setUpdatedAt( entity.getUpdatedAt() );

        channelResponse.setTypeText( entity.getTypeText() );
        channelResponse.setStatusText( entity.getStatusText() );
        channelResponse.setAlgorithmTypeText( getAlgorithmTypeText(entity.getAlgorithmType()) );
        channelResponse.setRtspUrl( maskRtspUrl(entity.getRtspUrl()) );

        return channelResponse;
    }

    @Override
    public List<ChannelResponse> toResponseList(List<Channel> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ChannelResponse> list = new ArrayList<ChannelResponse>( entities.size() );
        for ( Channel channel : entities ) {
            list.add( toResponse( channel ) );
        }

        return list;
    }
}
