-- 报警记录表
CREATE TABLE alarm_record (
    id                  BIGSERIAL PRIMARY KEY COMMENT '主键ID',
    type                SMALLINT NOT NULL COMMENT '报警类型：0-抽烟，1-PDI违规',
    site_id             BIGINT NOT NULL COMMENT '站点ID',
    channel_id          BIGINT NOT NULL COMMENT '通道ID',
    alarm_time          TIMESTAMP NOT NULL COMMENT '报警时间',
    location            VARCHAR(200) COMMENT '地点描述',
    face_image_url      VARCHAR(500) COMMENT '面部截图URL',
    scene_image_url     VARCHAR(500) COMMENT '场景截图URL',
    status              SMALLINT NOT NULL DEFAULT 0 COMMENT '状态：0-未处理，1-已处理，2-误报',
    extra_info          TEXT COMMENT '扩展信息（JSON格式）',
    processor_id        BIGINT COMMENT '处理人ID',
    processed_at        TIMESTAMP COMMENT '处理时间',
    remark              VARCHAR(500) COMMENT '处理备注',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at          TIMESTAMP COMMENT '逻辑删除时间',
    created_by          BIGINT COMMENT '创建人ID'
);

-- 表注释
COMMENT ON TABLE alarm_record IS '报警记录表';

-- 索引设计
CREATE INDEX idx_alarm_site_time ON alarm_record(site_id, alarm_time DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_alarm_status_site ON alarm_record(status, site_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_alarm_type ON alarm_record(type)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_alarm_channel ON alarm_record(channel_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_alarm_time ON alarm_record(alarm_time DESC)
    WHERE deleted_at IS NULL;

-- 复合索引：历史查询场景（站点+类型+时间）
CREATE INDEX idx_alarm_history_query ON alarm_record(site_id, type, alarm_time DESC)
    WHERE deleted_at IS NULL;

-- 处理人索引
CREATE INDEX idx_alarm_processor ON alarm_record(processor_id)
    WHERE deleted_at IS NULL AND processor_id IS NOT NULL;
