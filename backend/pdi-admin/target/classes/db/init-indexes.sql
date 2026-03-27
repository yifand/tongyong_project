-- ============================================================
-- PDI智能监测平台 - 索引创建脚本
-- 版本: 1.0.0
-- ============================================================

-- ============================================================
-- 索引创建
-- ============================================================

-- 系统表索引
CREATE INDEX IF NOT EXISTS idx_sys_user_site_id ON sys_user(site_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user(status) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_sur_role_id ON sys_user_role(role_id);
CREATE INDEX IF NOT EXISTS idx_srp_perm_id ON sys_role_permission(perm_id);

-- 设备表索引
CREATE INDEX IF NOT EXISTS idx_box_site_id ON box(site_id);
CREATE INDEX IF NOT EXISTS idx_box_status ON box(status);
CREATE INDEX IF NOT EXISTS idx_channel_box_id ON channel(box_id);
CREATE INDEX IF NOT EXISTS idx_channel_site_id ON channel(site_id);
CREATE INDEX IF NOT EXISTS idx_channel_type ON channel(channel_type);
CREATE INDEX IF NOT EXISTS idx_channel_status ON channel(status);

-- 业务表索引
CREATE INDEX IF NOT EXISTS idx_pdi_task_channel_id ON pdi_task(channel_id);
CREATE INDEX IF NOT EXISTS idx_pdi_task_site_id ON pdi_task(site_id);
CREATE INDEX IF NOT EXISTS idx_pdi_task_start_time ON pdi_task(start_time);
CREATE INDEX IF NOT EXISTS idx_pdi_task_status ON pdi_task(task_status);
CREATE INDEX IF NOT EXISTS idx_pdi_task_result ON pdi_task(task_result);

-- 报警表索引
CREATE INDEX IF NOT EXISTS idx_alarm_site_id ON alarm(site_id);
CREATE INDEX IF NOT EXISTS idx_alarm_channel_id ON alarm(channel_id);
CREATE INDEX IF NOT EXISTS idx_alarm_type ON alarm(alarm_type);
CREATE INDEX IF NOT EXISTS idx_alarm_status ON alarm(handle_status);
CREATE INDEX IF NOT EXISTS idx_alarm_time ON alarm(alarm_time DESC);
CREATE INDEX IF NOT EXISTS idx_alarm_pdi_task ON alarm(pdi_task_id);

-- 状态流表索引
CREATE INDEX IF NOT EXISTS idx_state_stream_channel_id ON state_stream(channel_id);
CREATE INDEX IF NOT EXISTS idx_state_stream_event_time ON state_stream(event_time DESC);
CREATE INDEX IF NOT EXISTS idx_state_stream_state ON state_stream(state_code);
CREATE INDEX IF NOT EXISTS idx_state_stream_processed ON state_stream(processed);

-- 操作日志索引
CREATE INDEX IF NOT EXISTS idx_oplog_user_id ON operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_oplog_operation ON operation_log(operation);
CREATE INDEX IF NOT EXISTS idx_oplog_module ON operation_log(module);
CREATE INDEX IF NOT EXISTS idx_oplog_ip ON operation_log(ip_address);
CREATE INDEX IF NOT EXISTS idx_oplog_time ON operation_log(log_time DESC);

-- 档案时间线索引
CREATE INDEX IF NOT EXISTS idx_archive_date ON archive_timeline(archive_date);
CREATE INDEX IF NOT EXISTS idx_archive_site_id ON archive_timeline(site_id);

-- 配置表索引
CREATE INDEX IF NOT EXISTS idx_config_category ON system_config(category);

-- ============================================================
-- 创建更新时间的触发器函数
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为所有表添加更新时间触发器
CREATE TRIGGER update_sys_user_updated_at BEFORE UPDATE ON sys_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_role_updated_at BEFORE UPDATE ON sys_role
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_permission_updated_at BEFORE UPDATE ON sys_permission
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_site_updated_at BEFORE UPDATE ON site
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_box_updated_at BEFORE UPDATE ON box
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_channel_updated_at BEFORE UPDATE ON channel
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pdi_task_updated_at BEFORE UPDATE ON pdi_task
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alarm_updated_at BEFORE UPDATE ON alarm
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_archive_timeline_updated_at BEFORE UPDATE ON archive_timeline
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_config_updated_at BEFORE UPDATE ON system_config
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 文档结束
-- ============================================================
