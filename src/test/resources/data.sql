-- 清空表（按外键顺序）
DELETE FROM access_logs;
DELETE FROM group_resource_permissions;
DELETE FROM badges;
DELETE FROM permission_groups;
DELETE FROM resources;
DELETE FROM employees;

-- 插入测试数据
INSERT INTO employees (employee_id, name) VALUES 
('E-0001', '张三'),
('E-0002', '李四');

INSERT INTO badges (badge_id, employee_id, status) VALUES 
('B-10001', 'E-0001', 'ACTIVE'),
('B-10002', 'E-0002', 'LOST');

INSERT INTO resources (resource_id, name, type, state) VALUES 
('R-DOOR-301', '301室门', 'DOOR', 'AVAILABLE'),
('R-PRINTER-2F', '2楼打印机', 'PRINTER', 'OCCUPIED');

INSERT INTO permission_groups (group_id, group_name) VALUES 
('G-DEV', '开发组');

INSERT INTO group_resource_permissions (group_id, resource_id) VALUES 
('G-DEV', 'R-DOOR-301');

-- 插入一条访问日志（成功）
INSERT INTO access_logs (timestamp, badge_id, employee_id, resource_id, decision, reason_code)
VALUES (NOW(), 'B-10001', 'E-0001', 'R-DOOR-301', 'ALLOW', 'ALLOW');

-- 插入一条访问日志（失败：徽章挂失）
INSERT INTO access_logs (timestamp, badge_id, employee_id, resource_id, decision, reason_code)
VALUES (NOW(), 'B-10002', 'E-0002', 'R-DOOR-301', 'DENY', 'BADGE_INACTIVE');