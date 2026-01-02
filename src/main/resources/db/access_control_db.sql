-- 创建数据库
CREATE DATABASE IF NOT EXISTS access_control_db;
USE access_control_db;

-- 1. 先创建无循环依赖的基础表：权限组表
CREATE TABLE IF NOT EXISTS group_permissions (
    group_id VARCHAR(50) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- 2. 资源表（无外键依赖，优先创建）
CREATE TABLE IF NOT EXISTS resources (
    resource_id VARCHAR(50) NOT NULL PRIMARY KEY,
    resource_name VARCHAR(100) NOT NULL,
    resource_type ENUM('DOOR', 'PRINTER', 'COMPUTER', 'ROOM', 'OTHER') NOT NULL,
    resource_state ENUM('AVAILABLE', 'OCCUPIED', 'LOCKED', 'OFFLINE') NOT NULL
);

-- 3. 先创建 employees 表（无外键，后续追加 badge_id 外键）
CREATE TABLE IF NOT EXISTS employees (
    employee_id VARCHAR(50) NOT NULL PRIMARY KEY,
    employee_name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) -- 先不建外键
);

-- 4. 创建 badges 表（引用 employees 的外键，此时 employees 已存在）
CREATE TABLE IF NOT EXISTS badges (
    badge_id VARCHAR(50) NOT NULL PRIMARY KEY,
    status ENUM('ACTIVE', 'DISABLED', 'LOST') NOT NULL,
    employee_id VARCHAR(50),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE SET NULL
);

-- 5. 给 employees 表追加 badge_id 外键（解决循环依赖）
ALTER TABLE employees 
ADD CONSTRAINT fk_employee_badge 
FOREIGN KEY (badge_id) REFERENCES badges(badge_id) ON DELETE CASCADE;

-- 6. 员工-组关联表（多对多，引用已存在的 group_permissions）
CREATE TABLE IF NOT EXISTS employee_groups (
    employee_id VARCHAR(50) NOT NULL,
    group_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (employee_id, group_id),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES group_permissions(group_id) ON DELETE CASCADE
);

-- 7. 组-资源关联表（多对多，引用已存在的 group_permissions）
CREATE TABLE IF NOT EXISTS group_resources (
    group_id VARCHAR(50) NOT NULL,
    resource_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id, resource_id),
    FOREIGN KEY (group_id) REFERENCES group_permissions(group_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
);

-- 8. 访问日志表
CREATE TABLE IF NOT EXISTS access_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    badge_id VARCHAR(50),
    employee_id VARCHAR(50),
    resource_id VARCHAR(50) NOT NULL,
    decision ENUM('ALLOW', 'DENY') NOT NULL,
    reason_code ENUM(
        'ALLOW', 
        'BADGE_NOT_FOUND', 
        'BADGE_INACTIVE', 
        'EMPLOYEE_NOT_FOUND', 
        'RESOURCE_NOT_FOUND', 
        'RESOURCE_LOCKED', 
        'RESOURCE_OCCUPIED', 
        'NO_PERMISSION', 
        'INVALID_REQUEST', 
        'SYSTEM_ERROR'
    ) NOT NULL,
    FOREIGN KEY (badge_id) REFERENCES badges(badge_id) ON DELETE SET NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE SET NULL,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
);

-- 创建索引（仅保留非主键的有效索引）
CREATE INDEX idx_group_id ON group_permissions(group_id); 
CREATE INDEX idx_log_badge ON access_logs(badge_id);
CREATE INDEX idx_log_employee ON access_logs(employee_id);
CREATE INDEX idx_log_resource ON access_logs(resource_id);
CREATE INDEX idx_log_timestamp ON access_logs(timestamp);
CREATE INDEX idx_log_decision ON access_logs(decision);