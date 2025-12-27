-- 创建数据库
CREATE DATABASE IF NOT EXISTS bigcomp;
USE bigcomp;

-- 员工表 (对应 AdminService.registerEmployee)
CREATE TABLE employees (
    employee_id VARCHAR(20) PRIMARY KEY COMMENT '员工ID (业务唯一标识)',
    name VARCHAR(100) NOT NULL COMMENT '员工姓名'
) COMMENT '系统员工信息';

-- 徽章表 (对应 AdminService.issueBadge, setBadgeStatus)
CREATE TABLE badges (
    badge_id VARCHAR(50) PRIMARY KEY COMMENT '徽章ID (如: B-10001)',
    employee_id VARCHAR(20) NOT NULL COMMENT '关联员工ID',
    status ENUM('ACTIVE', 'DISABLED', 'LOST') NOT NULL DEFAULT 'ACTIVE' COMMENT '徽章状态',
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
) COMMENT '访问徽章信息';

-- 资源表 (对应 AdminService.registerResource, setResourceState)
CREATE TABLE resources (
    resource_id VARCHAR(50) PRIMARY KEY COMMENT '资源ID (如: D-3F-201)',
    name VARCHAR(100) NOT NULL COMMENT '资源名称',
    type ENUM('DOOR', 'PRINTER', 'COMPUTER', 'ROOM', 'OTHER') NOT NULL COMMENT '资源类型',
    state ENUM('AVAILABLE', 'OCCUPIED', 'LOCKED', 'OFFLINE') NOT NULL DEFAULT 'AVAILABLE' COMMENT '资源状态'
) COMMENT '受控资源信息';

-- 组表 (对应 AdminService.createGroup)
CREATE TABLE permission_groups (
    group_id VARCHAR(20) PRIMARY KEY COMMENT '组ID',
    group_name VARCHAR(100) NOT NULL COMMENT '组名称'
) COMMENT '权限组信息';

-- 组资源权限表 (对应 AdminService.grantGroupAccessToResource, revokeGroupAccessToResource)
CREATE TABLE group_resource_permissions (
    group_id VARCHAR(20) NOT NULL,
    resource_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id, resource_id),
    FOREIGN KEY (group_id) REFERENCES permission_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
) COMMENT '组与资源的权限关系';

-- 访问日志表 (对应 LogEntry, LogQueryService)
CREATE TABLE access_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    timestamp TIMESTAMP NOT NULL COMMENT '访问发生时间',
    badge_id VARCHAR(50) NOT NULL COMMENT '徽章ID',
    employee_id VARCHAR(20) COMMENT '员工ID (可为空)',
    resource_id VARCHAR(50) NOT NULL COMMENT '资源ID',
    decision ENUM('ALLOW', 'DENY') NOT NULL COMMENT '访问决策',
    reason_code ENUM('ALLOW', 'BADGE_NOT_FOUND', 'BADGE_INACTIVE', 'EMPLOYEE_NOT_FOUND', 'RESOURCE_NOT_FOUND', 'RESOURCE_LOCKED', 'RESOURCE_OCCUPIED', 'NO_PERMISSION', 'INVALID_REQUEST', 'SYSTEM_ERROR') NOT NULL COMMENT '原因码',
    FOREIGN KEY (badge_id) REFERENCES badges(badge_id) ON DELETE RESTRICT,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE SET NULL,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE RESTRICT,
    INDEX idx_timestamp(timestamp),
    INDEX idx_badge(badge_id),
    INDEX idx_resource(resource_id),
    INDEX idx_employee(employee_id)
) COMMENT '访问日志记录';