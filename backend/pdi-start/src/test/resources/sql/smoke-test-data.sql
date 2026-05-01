-- ============================================================
-- PDI智能监测平台 - 冒烟测试数据
-- ============================================================

-- 插入测试站点
INSERT INTO site (id, name, code, site_id, created_at, created_by) VALUES
(1, '金桥库', 'JQ', 1, CURRENT_TIMESTAMP, 1),
(2, '凯迪库', 'KD', 2, CURRENT_TIMESTAMP, 1);

-- 插入测试用户 (密码: Test1234, BCrypt加密)
INSERT INTO sys_user (id, username, password, real_name, phone, email, status, site_id, created_at, created_by) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', '13800138000', 'admin@vdc.com', 1, 0, CURRENT_TIMESTAMP, 1),
(2, 'test_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '测试用户', '13800138001', 'test@vdc.com', 1, 1, CURRENT_TIMESTAMP, 1);

-- 插入测试角色
INSERT INTO sys_role (id, name, code, description, site_id, created_at, created_by) VALUES
(1, '超级管理员', 'ROLE_ADMIN', '系统超级管理员', 0, CURRENT_TIMESTAMP, 1),
(2, '普通用户', 'ROLE_USER', '普通业务用户', 0, CURRENT_TIMESTAMP, 1);

-- 插入用户角色关联
INSERT INTO sys_user_role (id, user_id, role_id, site_id, created_at, created_by) VALUES
(1, 1, 1, 0, CURRENT_TIMESTAMP, 1),
(2, 2, 2, 1, CURRENT_TIMESTAMP, 1);

-- 插入测试边缘盒子
INSERT INTO edge_box (id, site_id, name, ip_address, status, version, created_at, created_by) VALUES
(1, 1, '金桥盒子01', '192.168.1.101', 1, 'v2.1.0', CURRENT_TIMESTAMP, 1),
(2, 1, '金桥盒子02', '192.168.1.102', 1, 'v2.1.0', CURRENT_TIMESTAMP, 1),
(3, 2, '凯迪盒子01', '192.168.2.101', 1, 'v2.1.0', CURRENT_TIMESTAMP, 1);

-- 插入测试通道
INSERT INTO channel (id, box_id, site_id, name, type, status, algorithm_type, rtsp_url, created_at, created_by) VALUES
(1, 1, 1, '左前门通道', 0, 1, 'pdi_left_front', 'rtsp://192.168.1.101/stream1', CURRENT_TIMESTAMP, 1),
(2, 1, 1, '左后门通道', 0, 1, 'pdi_left_rear', 'rtsp://192.168.1.101/stream2', CURRENT_TIMESTAMP, 1),
(3, 1, 1, '滑门通道', 0, 1, 'pdi_slide', 'rtsp://192.168.1.101/stream3', CURRENT_TIMESTAMP, 1),
(4, 2, 1, '抓拍机01', 1, 1, 'smoke', 'rtsp://192.168.1.102/stream1', CURRENT_TIMESTAMP, 1);

-- 插入测试PDI作业
INSERT INTO pdi_task (id, channel_id, site_id, start_time, end_time, estimated_duration, actual_duration, result, created_at, created_by) VALUES
(1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 30, 3600, 1, CURRENT_TIMESTAMP, 1),
(2, 2, 1, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 30, 2400, 1, CURRENT_TIMESTAMP, 1),
(3, 1, 1, CURRENT_TIMESTAMP - INTERVAL '30' MINUTE, NULL, 30, NULL, 0, CURRENT_TIMESTAMP, 1);

-- 插入测试报警记录
INSERT INTO alarm_record (id, type, site_id, channel_id, alarm_time, location, face_image_url, scene_image_url, status, extra_info, created_at, created_by) VALUES
(1, 0, 1, 4, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, '休息区A', 'http://minio/smoke/face1.jpg', 'http://minio/smoke/scene1.jpg', 0, '{"confidence": 0.92}', CURRENT_TIMESTAMP, 1),
(2, 1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 'PDI工位1', 'http://minio/pdi/face2.jpg', 'http://minio/pdi/scene2.jpg', 1, '{"duration": 1800}', CURRENT_TIMESTAMP, 1),
(3, 0, 2, 1, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, '休息区B', 'http://minio/smoke/face3.jpg', 'http://minio/smoke/scene3.jpg', 2, '{"confidence": 0.85}', CURRENT_TIMESTAMP, 1);
