-- ============================================================
-- PDI智能监测平台 - 数据库初始化脚本
-- 版本: 1.0.0
-- 创建日期: 2026-03-27
-- 数据库: PostgreSQL 16.x
-- ============================================================

-- 设置客户端编码
SET client_encoding = 'UTF8';

-- ============================================================
-- 1. 系统表
-- ============================================================

-- 1.1 站点表
CREATE TABLE IF NOT EXISTS site (
    id              BIGSERIAL PRIMARY KEY,
    site_code       VARCHAR(50) NOT NULL,
    site_name       VARCHAR(100) NOT NULL,
    site_type       SMALLINT DEFAULT 1,
    address         VARCHAR(255),
    contact_name    VARCHAR(50),
    contact_phone   VARCHAR(20),
    status          SMALLINT NOT NULL DEFAULT 1,
    sort_order      INT DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_site_code UNIQUE (site_code)
);

COMMENT ON TABLE site IS '站点表';
COMMENT ON COLUMN site.site_type IS '站点类型: 1-仓库, 2-其他';
COMMENT ON COLUMN site.status IS '状态: 0-禁用, 1-启用';

-- 1.2 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    real_name       VARCHAR(50),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    avatar          VARCHAR(255),
    site_id         BIGINT,
    status          SMALLINT NOT NULL DEFAULT 1,
    last_login_time TIMESTAMP,
    last_login_ip   VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    deleted         SMALLINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT uk_sys_user_phone UNIQUE (phone),
    CONSTRAINT fk_sys_user_site_id FOREIGN KEY (site_id) REFERENCES site(id)
);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN sys_user.deleted IS '删除标记: 0-未删除, 1-已删除';

-- 1.3 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGSERIAL PRIMARY KEY,
    role_code   VARCHAR(50) NOT NULL,
    role_name   VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    data_scope  SMALLINT DEFAULT 2,
    status      SMALLINT NOT NULL DEFAULT 1,
    sort_order  INT DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.data_scope IS '数据范围: 1-全部, 2-本站点, 3-本人';
COMMENT ON COLUMN sys_role.status IS '状态: 0-禁用, 1-启用';

-- 1.4 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT DEFAULT 0,
    perm_code   VARCHAR(100) NOT NULL,
    perm_name   VARCHAR(50) NOT NULL,
    perm_type   SMALLINT NOT NULL,
    path        VARCHAR(200),
    component   VARCHAR(200),
    icon        VARCHAR(50),
    sort_order  INT DEFAULT 0,
    status      SMALLINT NOT NULL DEFAULT 1,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_sys_permission_code UNIQUE (perm_code)
);

COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.perm_type IS '类型: 1-菜单, 2-按钮, 3-接口';
COMMENT ON COLUMN sys_permission.status IS '状态: 0-禁用, 1-启用';

-- 1.5 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_sur_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_sur_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- 1.6 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id          BIGSERIAL PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    perm_id     BIGINT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_role_perm UNIQUE (role_id, perm_id),
    CONSTRAINT fk_srp_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT fk_srp_perm_id FOREIGN KEY (perm_id) REFERENCES sys_permission(id)
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';

-- 1.7 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id              BIGSERIAL PRIMARY KEY,
    config_key      VARCHAR(100) NOT NULL,
    config_value    TEXT,
    config_type     VARCHAR(20) DEFAULT 'string',
    description     VARCHAR(255),
    category        VARCHAR(50) DEFAULT 'general',
    is_editable     SMALLINT DEFAULT 1,
    sort_order      INT DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_sys_config_key UNIQUE (config_key)
);

COMMENT ON TABLE system_config IS '系统配置表';
COMMENT ON COLUMN system_config.config_type IS '值类型: string, int, float, json, boolean';
COMMENT ON COLUMN system_config.category IS '配置分类: general, algorithm, retention, notification';
COMMENT ON COLUMN system_config.is_editable IS '是否可编辑: 0-否, 1-是';

-- ============================================================
-- 2. 设备表
-- ============================================================

-- 2.1 边缘盒子表
CREATE TABLE IF NOT EXISTS box (
    id              BIGSERIAL PRIMARY KEY,
    box_code        VARCHAR(50) NOT NULL,
    box_name        VARCHAR(100) NOT NULL,
    site_id         BIGINT NOT NULL,
    ip_address      VARCHAR(50),
    mac_address     VARCHAR(50),
    status          SMALLINT NOT NULL DEFAULT 0,
    cpu_usage       DECIMAL(5,2),
    memory_usage    DECIMAL(5,2),
    disk_usage      DECIMAL(5,2),
    gpu_usage       DECIMAL(5,2),
    last_heartbeat  TIMESTAMP,
    software_version VARCHAR(50),
    algorithm_version VARCHAR(50),
    description     VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_box_code UNIQUE (box_code),
    CONSTRAINT fk_box_site_id FOREIGN KEY (site_id) REFERENCES site(id)
);

COMMENT ON TABLE box IS '边缘盒子表';
COMMENT ON COLUMN box.status IS '状态: 0-离线, 1-在线, 2-故障';

-- 2.2 通道表
CREATE TABLE IF NOT EXISTS channel (
    id                  BIGSERIAL PRIMARY KEY,
    channel_code        VARCHAR(50) NOT NULL,
    channel_name        VARCHAR(100) NOT NULL,
    box_id              BIGINT NOT NULL,
    site_id             BIGINT NOT NULL,
    channel_type        SMALLINT NOT NULL DEFAULT 1,
    stream_url          VARCHAR(500),
    camera_ip           VARCHAR(50),
    camera_brand        VARCHAR(50),
    algorithm_config    JSONB,
    status              SMALLINT NOT NULL DEFAULT 1,
    is_recording        SMALLINT DEFAULT 0,
    recording_save_days INT DEFAULT 7,
    sort_order          INT DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_channel_code UNIQUE (channel_code),
    CONSTRAINT fk_channel_box_id FOREIGN KEY (box_id) REFERENCES box(id),
    CONSTRAINT fk_channel_site_id FOREIGN KEY (site_id) REFERENCES site(id)
);

COMMENT ON TABLE channel IS '通道表';
COMMENT ON COLUMN channel.channel_type IS '通道类型: 1-PDI检测, 2-吸烟检测';
COMMENT ON COLUMN channel.status IS '状态: 0-禁用, 1-启用, 2-故障';
COMMENT ON COLUMN channel.is_recording IS '是否录像: 0-否, 1-是';

-- ============================================================
-- 3. 业务表
-- ============================================================

-- 3.1 PDI作业表
CREATE TABLE IF NOT EXISTS pdi_task (
    id                  BIGSERIAL PRIMARY KEY,
    task_no             VARCHAR(50) NOT NULL,
    channel_id          BIGINT NOT NULL,
    box_id              BIGINT NOT NULL,
    site_id             BIGINT NOT NULL,
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP,
    duration_seconds    INT,
    standard_duration   INT DEFAULT 1800,
    enter_state_seq     TEXT,
    exit_state_seq      TEXT,
    task_status         SMALLINT DEFAULT 1,
    task_result         SMALLINT,
    start_image_url     VARCHAR(500),
    end_image_url       VARCHAR(500),
    video_url           VARCHAR(500),
    remark              VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_pdi_task_no UNIQUE (task_no),
    CONSTRAINT fk_pdi_channel_id FOREIGN KEY (channel_id) REFERENCES channel(id),
    CONSTRAINT fk_pdi_box_id FOREIGN KEY (box_id) REFERENCES box(id),
    CONSTRAINT fk_pdi_site_id FOREIGN KEY (site_id) REFERENCES site(id)
);

COMMENT ON TABLE pdi_task IS 'PDI作业表';
COMMENT ON COLUMN pdi_task.task_status IS '状态: 1-进行中, 2-已完成, 3-异常中断';
COMMENT ON COLUMN pdi_task.task_result IS '结果: 1-合格, 2-超时, 3-异常';

-- 3.2 报警记录表（分区表）
CREATE TABLE IF NOT EXISTS alarm (
    id              BIGSERIAL,
    alarm_time      TIMESTAMP NOT NULL,
    alarm_type      SMALLINT NOT NULL,
    alarm_level     SMALLINT DEFAULT 2,
    site_id         BIGINT NOT NULL,
    box_id          BIGINT NOT NULL,
    channel_id      BIGINT NOT NULL,
    site_name       VARCHAR(100),
    channel_name    VARCHAR(100),
    pdi_task_id     BIGINT,
    alarm_title     VARCHAR(200) NOT NULL,
    alarm_desc      VARCHAR(500),
    image_url       VARCHAR(500),
    video_url       VARCHAR(500),
    handle_status   SMALLINT DEFAULT 0,
    handle_time     TIMESTAMP,
    handle_user_id  BIGINT,
    handle_remark   VARCHAR(500),
    extra_data      JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, alarm_time)
) PARTITION BY RANGE (alarm_time);

COMMENT ON TABLE alarm IS '报警记录表';
COMMENT ON COLUMN alarm.alarm_type IS '类型: 1-PDI超时, 2-违规吸烟, 3-门异常开启, 4-人员异常';
COMMENT ON COLUMN alarm.alarm_level IS '级别: 1-低, 2-中, 3-高, 4-紧急';
COMMENT ON COLUMN alarm.handle_status IS '处理状态: 0-未处理, 1-已确认, 2-已处理, 3-误报';

-- 创建报警表分区（按月分区）
CREATE TABLE IF NOT EXISTS alarm_2026_03 PARTITION OF alarm
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
CREATE TABLE IF NOT EXISTS alarm_2026_04 PARTITION OF alarm
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE IF NOT EXISTS alarm_2026_05 PARTITION OF alarm
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE IF NOT EXISTS alarm_2026_06 PARTITION OF alarm
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

-- 3.3 状态流表（分区表）
CREATE TABLE IF NOT EXISTS state_stream (
    id              BIGSERIAL,
    event_time      TIMESTAMP NOT NULL,
    channel_id      BIGINT NOT NULL,
    box_id          BIGINT NOT NULL,
    site_id         BIGINT NOT NULL,
    door_open       SMALLINT NOT NULL,
    person_present  SMALLINT NOT NULL,
    person_entering_exiting SMALLINT NOT NULL,
    state_code      VARCHAR(5) NOT NULL,
    door_confidence     DECIMAL(4,3),
    person_confidence   DECIMAL(4,3),
    frame_id        BIGINT,
    image_url       VARCHAR(500),
    processed       SMALLINT DEFAULT 0,

    PRIMARY KEY (id, event_time)
) PARTITION BY RANGE (event_time);

COMMENT ON TABLE state_stream IS '状态流表';
COMMENT ON COLUMN state_stream.state_code IS '状态编码: S1,S3,S5,S7,S8';
COMMENT ON COLUMN state_stream.processed IS '是否已处理: 0-未处理, 1-已处理';

-- 创建状态流表分区（按天分区）
CREATE TABLE IF NOT EXISTS state_stream_2026_03_27 PARTITION OF state_stream
    FOR VALUES FROM ('2026-03-27') TO ('2026-03-28');
CREATE TABLE IF NOT EXISTS state_stream_2026_03_28 PARTITION OF state_stream
    FOR VALUES FROM ('2026-03-28') TO ('2026-03-29');
CREATE TABLE IF NOT EXISTS state_stream_2026_03_29 PARTITION OF state_stream
    FOR VALUES FROM ('2026-03-29') TO ('2026-03-30');
CREATE TABLE IF NOT EXISTS state_stream_2026_03_30 PARTITION OF state_stream
    FOR VALUES FROM ('2026-03-30') TO ('2026-03-31');
CREATE TABLE IF NOT EXISTS state_stream_2026_03_31 PARTITION OF state_stream
    FOR VALUES FROM ('2026-03-31') TO ('2026-04-01');

-- 3.4 档案时间线表
CREATE TABLE IF NOT EXISTS archive_timeline (
    id              BIGSERIAL PRIMARY KEY,
    archive_date    DATE NOT NULL,
    site_id         BIGINT NOT NULL,
    channel_id      BIGINT NOT NULL,
    total_tasks     INT DEFAULT 0,
    completed_tasks INT DEFAULT 0,
    timeout_tasks   INT DEFAULT 0,
    total_alarms    INT DEFAULT 0,
    smoking_alarms  INT DEFAULT 0,
    timeline_data   JSONB,
    archive_file_url VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_archive_date_site_channel UNIQUE (archive_date, site_id, channel_id)
);

COMMENT ON TABLE archive_timeline IS '档案时间线表';

-- 3.5 操作日志表（分区表）
CREATE TABLE IF NOT EXISTS operation_log (
    id          BIGSERIAL,
    log_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id     BIGINT,
    username    VARCHAR(50),
    operation   VARCHAR(100) NOT NULL,
    module      VARCHAR(50),
    method      VARCHAR(200),
    request_url VARCHAR(500),
    request_params TEXT,
    response_data TEXT,
    ip_address  VARCHAR(50),
    user_agent  VARCHAR(500),
    execute_time INT,
    status      SMALLINT,
    error_msg   TEXT,

    PRIMARY KEY (id, log_time)
) PARTITION BY RANGE (log_time);

COMMENT ON TABLE operation_log IS '操作日志表';
COMMENT ON COLUMN operation_log.status IS '状态: 0-失败, 1-成功';

-- 创建操作日志表分区（按月分区）
CREATE TABLE IF NOT EXISTS operation_log_2026_03 PARTITION OF operation_log
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
CREATE TABLE IF NOT EXISTS operation_log_2026_04 PARTITION OF operation_log
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE IF NOT EXISTS operation_log_2026_05 PARTITION OF operation_log
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE IF NOT EXISTS operation_log_2026_06 PARTITION OF operation_log
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
