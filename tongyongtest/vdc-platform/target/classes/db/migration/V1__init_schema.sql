-- VDC Platform Initial Schema
-- PostgreSQL 15+

-- site
CREATE TABLE IF NOT EXISTS site (
    id BIGSERIAL PRIMARY KEY,
    site_code VARCHAR(32) NOT NULL UNIQUE,
    site_name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- edge_box
CREATE TABLE IF NOT EXISTS edge_box (
    id BIGSERIAL PRIMARY KEY,
    box_id VARCHAR(64) NOT NULL UNIQUE,
    box_name VARCHAR(64),
    site_id BIGINT NOT NULL REFERENCES site(id),
    ip_address VARCHAR(32),
    secret_key VARCHAR(128),
    status SMALLINT NOT NULL DEFAULT 0,
    last_heartbeat TIMESTAMP,
    version VARCHAR(32),
    cpu_usage DECIMAL(5,2),
    mem_usage DECIMAL(5,2),
    disk_usage DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- channel
CREATE TABLE IF NOT EXISTS channel (
    id BIGSERIAL PRIMARY KEY,
    channel_id VARCHAR(64) NOT NULL,
    channel_name VARCHAR(64),
    box_id BIGINT NOT NULL REFERENCES edge_box(id),
    channel_type VARCHAR(32),
    status SMALLINT NOT NULL DEFAULT 0,
    algorithm_type VARCHAR(32),
    rtsp_url VARCHAR(256),
    username VARCHAR(64),
    password VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- sys_role
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(32) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL,
    permissions JSONB,
    data_scope VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- sys_user
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    real_name VARCHAR(64),
    phone VARCHAR(16),
    email VARCHAR(64),
    role_id BIGINT NOT NULL REFERENCES sys_role(id),
    site_id BIGINT REFERENCES site(id),
    status SMALLINT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- state_stream (partitioned by range on ts)
CREATE TABLE IF NOT EXISTS state_stream (
    id BIGSERIAL,
    box_id VARCHAR(64) NOT NULL,
    channel_id VARCHAR(64) NOT NULL,
    ts TIMESTAMP NOT NULL,
    door_open BOOLEAN NOT NULL,
    person_present BOOLEAN NOT NULL,
    person_entering_exiting BOOLEAN NOT NULL,
    vehicle_present BOOLEAN NOT NULL,
    state_combination SMALLINT NOT NULL,
    snapshot_target VARCHAR(256),
    snapshot_scene VARCHAR(256),
    PRIMARY KEY (id, ts)
) PARTITION BY RANGE (ts);

-- work_session
CREATE TABLE IF NOT EXISTS work_session (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES site(id),
    channel_id BIGINT NOT NULL REFERENCES channel(id),
    vehicle_info VARCHAR(64),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    actual_duration INT,
    standard_duration INT NOT NULL,
    deviation_pct DECIMAL(5,2),
    result VARCHAR(16),
    snapshot_head VARCHAR(256),
    snapshot_tail VARCHAR(256),
    snapshot_mid VARCHAR(256),
    status SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- alarm
CREATE TABLE IF NOT EXISTS alarm (
    id BIGSERIAL PRIMARY KEY,
    alarm_type VARCHAR(32) NOT NULL,
    site_id BIGINT NOT NULL REFERENCES site(id),
    channel_id BIGINT NOT NULL REFERENCES channel(id),
    work_session_id BIGINT REFERENCES work_session(id),
    alarm_time TIMESTAMP NOT NULL,
    process_status VARCHAR(16) NOT NULL DEFAULT 'UNPROCESSED',
    processed_by BIGINT REFERENCES sys_user(id),
    processed_at TIMESTAMP,
    target_image VARCHAR(256),
    scene_image VARCHAR(256),
    watermark_logo VARCHAR(128),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- operation_log
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id),
    username VARCHAR(64) NOT NULL,
    ip_address VARCHAR(32),
    operation_type VARCHAR(32) NOT NULL,
    operation_content TEXT,
    result SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- rule_config
CREATE TABLE IF NOT EXISTS rule_config (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(64) NOT NULL,
    channel_type VARCHAR(32) NOT NULL,
    require_vehicle BOOLEAN NOT NULL DEFAULT true,
    enter_pattern JSONB NOT NULL,
    exit_pattern JSONB NOT NULL,
    standard_duration INT NOT NULL,
    critical_threshold_pct DECIMAL(5,2) NOT NULL DEFAULT 90.00,
    person_absent_timeout INT,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- system_config
CREATE TABLE IF NOT EXISTS system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(256),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_state_stream_channel_ts ON state_stream (channel_id, ts DESC);
CREATE INDEX IF NOT EXISTS idx_alarm_site_time_type ON alarm (site_id, alarm_time DESC, alarm_type);
CREATE INDEX IF NOT EXISTS idx_work_session_site_channel_start ON work_session (site_id, channel_id, start_time DESC);

-- Create partitions for current month + next 3 months
DO $$
DECLARE
    start_date DATE := DATE_TRUNC('month', CURRENT_DATE);
    end_date DATE;
    partition_name TEXT;
    start_str TEXT;
    end_str TEXT;
    i INT;
BEGIN
    FOR i IN 0..3 LOOP
        start_date := DATE_TRUNC('month', CURRENT_DATE + (i || ' months')::INTERVAL);
        end_date := start_date + INTERVAL '1 month';
        partition_name := 'state_stream_' || TO_CHAR(start_date, 'YYYY_MM');
        start_str := TO_CHAR(start_date, 'YYYY-MM-DD');
        end_str := TO_CHAR(end_date, 'YYYY-MM-DD');
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS %I PARTITION OF state_stream FOR VALUES FROM (%L) TO (%L)',
            partition_name, start_str, end_str
        );
    END LOOP;
END $$;
