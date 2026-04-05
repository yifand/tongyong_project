package com.vdc.pdi.behaviorarchive.mapper;

import com.vdc.pdi.behaviorarchive.domain.entity.BehaviorArchive;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveDetailResponse;
import com.vdc.pdi.behaviorarchive.dto.response.ArchiveResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-06T00:52:02+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ArchiveMapperImpl implements ArchiveMapper {

    @Override
    public ArchiveResponse toResponse(BehaviorArchive archive) {
        if ( archive == null ) {
            return null;
        }

        ArchiveResponse archiveResponse = new ArchiveResponse();

        archiveResponse.setStatusText( statusToText( archive.getStatus() ) );
        archiveResponse.setId( archive.getId() );
        archiveResponse.setPdiTaskId( archive.getPdiTaskId() );
        archiveResponse.setChannelId( archive.getChannelId() );
        archiveResponse.setSiteId( archive.getSiteId() );
        archiveResponse.setStartTime( archive.getStartTime() );
        archiveResponse.setEndTime( archive.getEndTime() );
        archiveResponse.setEstimatedDuration( archive.getEstimatedDuration() );
        archiveResponse.setActualDuration( archive.getActualDuration() );
        archiveResponse.setStatus( archive.getStatus() );
        archiveResponse.setCreatedAt( archive.getCreatedAt() );

        return archiveResponse;
    }

    @Override
    public List<ArchiveResponse> toResponseList(List<BehaviorArchive> archives) {
        if ( archives == null ) {
            return null;
        }

        List<ArchiveResponse> list = new ArrayList<ArchiveResponse>( archives.size() );
        for ( BehaviorArchive behaviorArchive : archives ) {
            list.add( toResponse( behaviorArchive ) );
        }

        return list;
    }

    @Override
    public ArchiveDetailResponse toDetailResponse(BehaviorArchive archive) {
        if ( archive == null ) {
            return null;
        }

        ArchiveDetailResponse archiveDetailResponse = new ArchiveDetailResponse();

        archiveDetailResponse.setStatusText( statusToText( archive.getStatus() ) );
        archiveDetailResponse.setId( archive.getId() );
        archiveDetailResponse.setPdiTaskId( archive.getPdiTaskId() );
        archiveDetailResponse.setStartTime( archive.getStartTime() );
        archiveDetailResponse.setEndTime( archive.getEndTime() );
        archiveDetailResponse.setEstimatedDuration( archive.getEstimatedDuration() );
        archiveDetailResponse.setActualDuration( archive.getActualDuration() );
        archiveDetailResponse.setStatus( archive.getStatus() );

        return archiveDetailResponse;
    }
}
