-- ============================================================
-- PDI智能监测平台 - 初始化数据脚本
-- 版本: 1.0.0
-- ============================================================

-- ============================================================
-- 1. 站点数据
-- ============================================================
INSERT INTO site (id, site_code, site_name, site_type, address, contact_name, contact_phone, status, sort_order) VALUES
(1, 'JINQIAO', '金桥库', 1, '金桥基地', '张三', '13800138001', 1, 1),
(2, 'KAIDI', '凯迪库', 1, '凯迪基地', '李四', '13800138002', 1, 2);

SELECT setval('site_id_seq', 2, true);

-- ============================================================
-- 2. 角色数据
-- ============================================================
INSERT INTO sys_role (id, role_code, role_name, description, data_scope, status, sort_order) VALUES
(1, 'admin', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1, 1),
(2, 'site_admin', '站点管理员', '站点级管理员，管理本站点数据', 2, 1, 2),
(3, 'operator', '操作员', '普通操作员，可查看和处理预警', 2, 1, 3),
(4, 'viewer', '查看员', '只读用户，仅能查看数据', 2, 1, 4);

SELECT setval('sys_role_id_seq', 4, true);

-- ============================================================
-- 3. 权限数据（菜单权限）
-- ============================================================
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, component, icon, sort_order, status) VALUES
-- 工作台
(1, 0, 'dashboard', '工作台', 1, '/dashboard', 'Dashboard', 'dashboard', 1, 1),

-- 预警中心
(10, 0, 'alarm', '预警中心', 1, '/alarm', NULL, 'warning', 2, 1),
(11, 10, 'alarm:realtime', '实时预警', 1, '/alarm/realtime', 'AlarmRealtime', NULL, 1, 1),
(12, 10, 'alarm:history', '历史预警', 1, '/alarm/history', 'AlarmHistory', NULL, 2, 1),
(13, 10, 'alarm:view', '查看预警', 3, NULL, NULL, NULL, 3, 1),
(14, 10, 'alarm:handle', '处理预警', 3, NULL, NULL, NULL, 4, 1),
(15, 10, 'alarm:export', '导出预警', 3, NULL, NULL, NULL, 5, 1),

-- 行为档案
(20, 0, 'archive', '行为档案', 1, '/archive', NULL, 'file-text', 3, 1),
(21, 20, 'archive:list', '档案列表', 1, '/archive/list', 'ArchiveList', NULL, 1, 1),
(22, 20, 'archive:view', '查看档案', 3, NULL, NULL, NULL, 2, 1),
(23, 20, 'archive:download', '下载档案', 3, NULL, NULL, NULL, 3, 1),

-- 设备管理
(30, 0, 'device', '设备管理', 1, '/device', NULL, 'monitor', 4, 1),
(31, 30, 'device:box', '盒子管理', 1, '/device/box', 'DeviceBox', NULL, 1, 1),
(32, 30, 'device:channel', '通道管理', 1, '/device/channel', 'DeviceChannel', NULL, 2, 1),
(33, 30, 'device:view', '查看设备', 3, NULL, NULL, NULL, 3, 1),
(34, 30, 'device:edit', '编辑设备', 3, NULL, NULL, NULL, 4, 1),
(35, 30, 'device:control', '控制设备', 3, NULL, NULL, NULL, 5, 1),

-- 系统管理
(40, 0, 'system', '系统管理', 1, '/system', NULL, 'setting', 5, 1),
(41, 40, 'system:user', '用户管理', 1, '/system/user', 'SystemUser', NULL, 1, 1),
(42, 40, 'system:role', '角色管理', 1, '/system/role', 'SystemRole', NULL, 2, 1),
(43, 40, 'system:log', '日志管理', 1, '/system/log', 'SystemLog', NULL, 3, 1),
(44, 40, 'system:config', '系统配置', 1, '/system/config', 'SystemConfig', NULL, 4, 1),
(45, 40, 'system:user:create', '创建用户', 3, NULL, NULL, NULL, 5, 1),
(46, 40, 'system:user:edit', '编辑用户', 3, NULL, NULL, NULL, 6, 1),
(47, 40, 'system:user:delete', '删除用户', 3, NULL, NULL, NULL, 7, 1),
(48, 40, 'system:role:create', '创建角色', 3, NULL, NULL, NULL, 8, 1),
(49, 40, 'system:role:edit', '编辑角色', 3, NULL, NULL, NULL, 9, 1);

SELECT setval('sys_permission_id_seq', 49, true);

-- ============================================================
-- 4. 角色权限关联
-- ============================================================
-- 超级管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 1, id FROM sys_permission WHERE status = 1;

-- 站点管理员权限
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(2, 1),   -- 工作台
(2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15),  -- 预警中心
(2, 20), (2, 21), (2, 22), (2, 23),  -- 行为档案
(2, 30), (2, 31), (2, 32), (2, 33), (2, 34), (2, 35),  -- 设备管理
(2, 40), (2, 41), (2, 42), (2, 43), (2, 44);  -- 系统管理

-- 操作员权限
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(3, 1),   -- 工作台
(3, 10), (3, 11), (3, 12), (3, 13), (3, 14),  -- 预警中心（不能导出）
(3, 20), (3, 21), (3, 22),  -- 行为档案（不能下载）
(3, 30), (3, 31), (3, 32), (3, 33);  -- 设备管理（只能查看）

-- 查看员权限
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(4, 1),   -- 工作台
(4, 10), (4, 11), (4, 12), (4, 13),  -- 预警中心（只能查看）
(4, 20), (4, 21), (4, 22),  -- 行为档案
(4, 30), (4, 31), (4, 32), (4, 33);  -- 设备管理（只能查看）

-- ============================================================
-- 5. 用户数据
-- ============================================================
-- 密码: admin123 (BCrypt加密)
INSERT INTO sys_user (id, username, password, real_name, phone, email, avatar, site_id, status, created_by) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', '13800138000', 'admin@pdi.com', NULL, NULL, 1, 1);

SELECT setval('sys_user_id_seq', 1, true);

-- ============================================================
-- 6. 用户角色关联
-- ============================================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);  -- admin -> 超级管理员

-- ============================================================
-- 7. 系统配置数据
-- ============================================================
INSERT INTO system_config (config_key, config_value, config_type, description, category, is_editable, sort_order) VALUES
-- 算法配置
('algorithm.pdi.confidence_threshold', '0.75', 'float', 'PDI检测置信度阈值', 'algorithm', 1, 1),
('algorithm.pdi.standard_duration', '1800', 'int', 'PDI标准时长（秒）', 'algorithm', 1, 2),
('algorithm.pdi.state_debounce_ms', '500', 'int', '状态去抖动时间（毫秒）', 'algorithm', 1, 3),
('algorithm.smoking.confidence_threshold', '0.80', 'float', '吸烟检测置信度阈值', 'algorithm', 1, 4),
('algorithm.smoking.consecutive_frames', '3', 'int', '吸烟检测连续触发帧数', 'algorithm', 1, 5),

-- 数据保留配置
('retention.alarm_days', '90', 'int', '报警记录保留天数', 'retention', 1, 10),
('retention.state_stream_days', '30', 'int', '状态流数据保留天数', 'retention', 1, 11),
('retention.log_days', '180', 'int', '操作日志保留天数', 'retention', 1, 12),
('retention.video_days', '7', 'int', '录像文件保留天数', 'retention', 1, 13),

-- 通知配置
('notification.sound_enabled', 'true', 'boolean', '报警声音开关', 'notification', 1, 20),
('notification.sound_volume', '80', 'int', '报警声音音量（0-100）', 'notification', 1, 21),

-- 系统配置
('system.login_max_retry', '5', 'int', '登录最大重试次数', 'general', 1, 30),
('system.login_lock_minutes', '5', 'int', '登录失败锁定时间（分钟）', 'general', 1, 31),
('system.password_expire_days', '90', 'int', '密码过期天数', 'general', 1, 32),
('system.token_expire_hours', '2', 'int', 'Token过期时间（小时）', 'general', 1, 33),
('system.refresh_token_expire_days', '7', 'int', '刷新Token过期时间（天）', 'general', 1, 34);

-- ============================================================
-- 8. 示例设备数据（可选）
-- ============================================================
-- 盒子数据
INSERT INTO box (id, box_code, box_name, site_id, ip_address, status, software_version, description) VALUES
(1, 'BOX001', '金桥库盒子01', 1, '192.168.1.101', 0, 'v1.2.0', '主通道盒子'),
(2, 'BOX002', '凯迪库盒子01', 2, '192.168.1.102', 0, 'v1.2.0', '主通道盒子');

SELECT setval('box_id_seq', 2, true);

-- 通道数据
INSERT INTO channel (id, channel_code, channel_name, box_id, site_id, channel_type, stream_url, camera_ip, camera_brand, algorithm_config, status, sort_order) VALUES
(1, 'CH001', '左前门通道', 1, 1, 1, 'rtsp://192.168.1.201:554/stream1', '192.168.1.201', '海康威视',
 '{"door_roi": [100, 200, 300, 400], "person_roi": [150, 250, 350, 450], "confidence_threshold": 0.75}', 1, 1),
(2, 'CH002', '左后门通道', 1, 1, 1, 'rtsp://192.168.1.202:554/stream1', '192.168.1.202', '海康威视',
 '{"door_roi": [100, 200, 300, 400], "person_roi": [150, 250, 350, 450], "confidence_threshold": 0.75}', 1, 2),
(3, 'CH003', '休息区A', 1, 1, 2, 'rtsp://192.168.1.203:554/stream1', '192.168.1.203', '海康威视',
 '{"smoking_roi": [100, 200, 300, 400], "confidence_threshold": 0.80}', 1, 3),
(4, 'CH004', '左前门通道', 2, 2, 1, 'rtsp://192.168.1.204:554/stream1', '192.168.1.204', '海康威视',
 '{"door_roi": [100, 200, 300, 400], "person_roi": [150, 250, 350, 450], "confidence_threshold": 0.75}', 1, 1),
(5, 'CH005', '休息区B', 2, 2, 2, 'rtsp://192.168.1.205:554/stream1', '192.168.1.205', '海康威视',
 '{"smoking_roi": [100, 200, 300, 400], "confidence_threshold": 0.80}', 1, 2);

SELECT setval('channel_id_seq', 5, true);

-- ============================================================
-- 文档结束
-- ============================================================
