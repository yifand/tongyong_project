package com.vdc.pdi.logmanagement.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSystemLog is a Querydsl query type for SystemLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSystemLog extends EntityPathBase<SystemLog> {

    private static final long serialVersionUID = 204004494L;

    public static final QSystemLog systemLog = new QSystemLog("systemLog");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final StringPath message = createString("message");

    public final StringPath module = createString("module");

    public final StringPath sourceClass = createString("sourceClass");

    public final StringPath sourceMethod = createString("sourceMethod");

    public final StringPath stackTrace = createString("stackTrace");

    public final StringPath threadName = createString("threadName");

    public QSystemLog(String variable) {
        super(SystemLog.class, forVariable(variable));
    }

    public QSystemLog(Path<? extends SystemLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSystemLog(PathMetadata metadata) {
        super(SystemLog.class, metadata);
    }

}

