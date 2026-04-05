package com.vdc.pdi.device.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEdgeBox is a Querydsl query type for EdgeBox
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEdgeBox extends EntityPathBase<EdgeBox> {

    private static final long serialVersionUID = 91868788L;

    public static final QEdgeBox edgeBox = new QEdgeBox("edgeBox");

    public final com.vdc.pdi.common.entity.QBaseEntity _super = new com.vdc.pdi.common.entity.QBaseEntity(this);

    public final NumberPath<Double> cpuUsage = createNumber("cpuUsage", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Double> diskUsage = createNumber("diskUsage", Double.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath ipAddress = createString("ipAddress");

    public final DateTimePath<java.time.LocalDateTime> lastHeartbeatAt = createDateTime("lastHeartbeatAt", java.time.LocalDateTime.class);

    public final NumberPath<Double> memoryUsage = createNumber("memoryUsage", Double.class);

    public final StringPath name = createString("name");

    //inherited
    public final NumberPath<Long> siteId = _super.siteId;

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath version = createString("version");

    public QEdgeBox(String variable) {
        super(EdgeBox.class, forVariable(variable));
    }

    public QEdgeBox(Path<? extends EdgeBox> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEdgeBox(PathMetadata metadata) {
        super(EdgeBox.class, metadata);
    }

}

