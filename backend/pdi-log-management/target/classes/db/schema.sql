-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id                  BIGSERIAL PRIMARY KEY,
    site_id             BIGINT NOT NULL DEFAULT 0,
    user_id             BIGINT NOT NULL,
    username            VARCHAR(64) NOT NULL,
    ip_address          VARCHAR(64),
    operation_type      SMALLINT NOT NULL,
    operation_detail    VARCHAR(512),
    request_params      VARCHAR(2048),
    result              SMALLINT NOT NULL DEFAULT 1,
    error_msg           VARCHAR(1024),
    execution_time      BIGINT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    deleted_at          TIMESTAMP
);

-- 索引设计
CREATE INDEX IF NOT EXISTS idx_op_log_site_id ON sys_operation_log(site_id);
CREATE INDEX IF NOT EXISTS idx_op_log_user_id ON sys_operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_op_log_type ON sys_operation_log(operation_type);
CREATE INDEX IF NOT EXISTS idx_op_log_result ON sys_operation_log(result);
CREATE INDEX IF NOT EXISTS idx_op_log_created ON sys_operation_log(created_at);
CREATE INDEX IF NOT EXISTS idx_op_log_user_created ON sys_operation_log(user_id, created_at);

-- 表注释
COMMENT ON TABLE sys_operation_log IS '操作日志表';
COMMENT ON COLUMN sys_operation_log.id IS '主键ID';
COMMENT ON COLUMN sys_operation_log.site_id IS '站点ID';
COMMENT ON COLUMN sys_operation_log.user_id IS '用户ID';
COMMENT ON COLUMN sys_operation_log.username IS '用户名';
COMMENT ON COLUMN sys_operation_log.ip_address IS 'IP地址';
COMMENT ON COLUMN sys_operation_log.operation_type IS '操作类型：1-登录 2-登出 3-配置修改 4-用户管理 5-角色管理 6-数据导出 7-报警处理 8-设备管理 9-阈值配置 99-其他';
COMMENT ON COLUMN sys_operation_log.operation_detail IS '操作详情描述';
COMMENT ON COLUMN sys_operation_log.request_params IS '请求参数JSON';
COMMENT ON COLUMN sys_operation_log.result IS '操作结果：0-失败 1-成功';
COMMENT ON COLUMN sys_operation_log.error_msg IS '错误信息';
COMMENT ON COLUMN sys_operation_log.execution_time IS '执行时长(毫秒)';
COMMENT ON COLUMN sys_operation_log.created_at IS '创建时间';

-- 系统日志表
CREATE TABLE IF NOT EXISTS system_log (
    id                  BIGSERIAL PRIMARY KEY,
    level               SMALLINT NOT NULL,
    module              VARCHAR(64) NOT NULL,
    message             VARCHAR(2048) NOT NULL,
    stack_trace         VARCHAR(4096),
    source_class        VARCHAR(256),
    source_method       VARCHAR(128),
    thread_name         VARCHAR(64),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引设计
CREATE INDEX IF NOT EXISTS idx_sys_log_level ON system_log(level);
CREATE INDEX IF NOT EXISTS idx_sys_log_module ON system_log(module);
CREATE INDEX IF NOT EXISTS idx_sys_log_created ON system_log(created_at);
CREATE INDEX IF NOT EXISTS idx_sys_log_level_created ON system_log(level, created_at);

-- 表注释
COMMENT ON TABLE system_log IS '系统日志表';
COMMENT ON COLUMN system_log.id IS '主键ID';
COMMENT ON COLUMN system_log.level IS '日志级别：0-DEBUG 1-INFO 2-WARN 3-ERROR';
COMMENT ON COLUMN system_log.module IS '模块名称';
COMMENT ON COLUMN system_log.message IS '日志消息';
COMMENT ON COLUMN system_log.stack_trace IS '异常堆栈';
COMMENT ON COLUMN system_log.source_class IS '来源类名';
COMMENT ON COLUMN system_log.source_method IS '来源方法名';
COMMENT ON COLUMN system_log.thread_name IS '线程名称';
COMMENT ON COLUMN system_log.created_at IS '创建时间';
