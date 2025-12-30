package acs.service;

import acs.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // 确保测试结束后回滚数据，不污染测试库
public class DatabaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 核心验证：领域模型枚举与数据库ENUM类型完全一致
     * 防止代码枚举与数据库定义不同步导致的映射错误
     */
    @Test
    void verifyEnumConsistencyBetweenDomainAndDatabase() {
        // 验证资源类型枚举
        verifyEnumMapping(
            ResourceType.class,
            "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_NAME = 'resources' AND COLUMN_NAME = 'type'",
            "DOOR", "PRINTER", "COMPUTER", "ROOM", "OTHER"
        );

        // 验证资源状态枚举
        verifyEnumMapping(
            ResourceState.class,
            "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_NAME = 'resources' AND COLUMN_NAME = 'state'",
            "AVAILABLE", "OCCUPIED", "LOCKED", "OFFLINE"
        );

        // 验证徽章状态枚举
        verifyEnumMapping(
            BadgeStatus.class,
            "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_NAME = 'badges' AND COLUMN_NAME = 'status'",
            "ACTIVE", "DISABLED", "LOST"
        );

        // 验证访问决策枚举
        verifyEnumMapping(
            AccessDecision.class,
            "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_NAME = 'access_logs' AND COLUMN_NAME = 'decision'",
            "ALLOW", "DENY"
        );

        // 验证原因码枚举
        verifyEnumMapping(
            ReasonCode.class,
            "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_NAME = 'access_logs' AND COLUMN_NAME = 'reason_code'",
            "ALLOW", "BADGE_NOT_FOUND", "BADGE_INACTIVE", "EMPLOYEE_NOT_FOUND",
            "RESOURCE_NOT_FOUND", "RESOURCE_LOCKED", "RESOURCE_OCCUPIED",
            "NO_PERMISSION", "INVALID_REQUEST", "SYSTEM_ERROR"
        );
    }

    /**
     * 验证员工表基础数据及结构
     */
    @Test
    void testEmployeesTable() {
        // 验证表结构
        List<String> employeeColumns = getTableColumns("employees");
        assertThat(employeeColumns).containsExactlyInAnyOrder("employee_id", "name");

        // 验证测试数据
        List<Map<String, Object>> employees = jdbcTemplate.queryForList("SELECT * FROM employees");
        assertThat(employees).hasSize(2);

        // 验证员工E-0001
        Map<String, Object> emp1 = findByKey(employees, "employee_id", "E-0001");
        assertThat(emp1).isNotNull();
        assertThat(emp1.get("name")).isEqualTo("张三");

        // 验证员工E-0002
        Map<String, Object> emp2 = findByKey(employees, "employee_id", "E-0002");
        assertThat(emp2).isNotNull();
        assertThat(emp2.get("name")).isEqualTo("李四");
    }

    /**
     * 验证徽章表数据及外键关系
     */
    @Test
    void testBadgesTable() {
        // 验证表结构
        List<String> badgeColumns = getTableColumns("badges");
        assertThat(badgeColumns).containsExactlyInAnyOrder("badge_id", "employee_id", "status");

        // 验证测试数据
        List<Map<String, Object>> badges = jdbcTemplate.queryForList("SELECT * FROM badges");
        assertThat(badges).hasSize(2);

        // 验证有效徽章B-10001
        Map<String, Object> activeBadge = findByKey(badges, "badge_id", "B-10001");
        assertThat(activeBadge).isNotNull();
        assertThat(activeBadge.get("employee_id")).isEqualTo("E-0001");
        assertThat(activeBadge.get("status")).isEqualTo("ACTIVE");

        // 验证挂失徽章B-10002
        Map<String, Object> lostBadge = findByKey(badges, "badge_id", "B-10002");
        assertThat(lostBadge).isNotNull();
        assertThat(lostBadge.get("employee_id")).isEqualTo("E-0002");
        assertThat(lostBadge.get("status")).isEqualTo("LOST");

        // 验证外键约束（尝试删除被引用的员工应失败）
        assertThatThrownBy(() -> 
            jdbcTemplate.update("DELETE FROM employees WHERE employee_id = 'E-0001'")
        ).isNotNull();
    }

    /**
     * 验证资源表数据
     */
    @Test
    void testResourcesTable() {
        List<Map<String, Object>> resources = jdbcTemplate.queryForList("SELECT * FROM resources");
        assertThat(resources).hasSize(2);

        // 验证301室门
        Map<String, Object> door = findByKey(resources, "resource_id", "R-DOOR-301");
        assertThat(door).isNotNull();
        assertThat(door.get("name")).isEqualTo("301室门");
        assertThat(door.get("type")).isEqualTo("DOOR");
        assertThat(door.get("state")).isEqualTo("AVAILABLE");

        // 验证2楼打印机
        Map<String, Object> printer = findByKey(resources, "resource_id", "R-PRINTER-2F");
        assertThat(printer).isNotNull();
        assertThat(printer.get("name")).isEqualTo("2楼打印机");
        assertThat(printer.get("type")).isEqualTo("PRINTER");
        assertThat(printer.get("state")).isEqualTo("OCCUPIED");
    }

    /**
     * 验证权限组及资源权限关联
     */
    @Test
    void testPermissionGroupsAndPermissions() {
        // 验证权限组
        List<Map<String, Object>> groups = jdbcTemplate.queryForList("SELECT * FROM permission_groups");
        assertThat(groups).hasSize(1);
        
        Map<String, Object> devGroup = findByKey(groups, "group_id", "G-DEV");
        assertThat(devGroup).isNotNull();
        assertThat(devGroup.get("group_name")).isEqualTo("开发组");

        // 验证组资源权限
        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(
            "SELECT * FROM group_resource_permissions"
        );
        assertThat(permissions).hasSize(1);
        
        Map<String, Object> doorPermission = findByKey(permissions, "group_id", "G-DEV");
        assertThat(doorPermission).isNotNull();
        assertThat(doorPermission.get("resource_id")).isEqualTo("R-DOOR-301");
    }

    /**
     * 验证访问日志表数据
     */
    @Test
    void testAccessLogsTable() {
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM access_logs ORDER BY timestamp"
        );
        assertThat(logs).hasSize(2);

        // 验证允许访问的日志
        Map<String, Object> allowLog = logs.get(0);
        assertThat(allowLog.get("badge_id")).isEqualTo("B-10001");
        assertThat(allowLog.get("employee_id")).isEqualTo("E-0001");
        assertThat(allowLog.get("resource_id")).isEqualTo("R-DOOR-301");
        assertThat(allowLog.get("decision")).isEqualTo("ALLOW");
        assertThat(allowLog.get("reason_code")).isEqualTo("ALLOW");

        // 验证拒绝访问的日志（徽章挂失）
        Map<String, Object> denyLog = logs.get(1);
        assertThat(denyLog.get("badge_id")).isEqualTo("B-10002");
        assertThat(denyLog.get("employee_id")).isEqualTo("E-0002");
        assertThat(denyLog.get("resource_id")).isEqualTo("R-DOOR-301");
        assertThat(denyLog.get("decision")).isEqualTo("DENY");
        assertThat(denyLog.get("reason_code")).isEqualTo("BADGE_INACTIVE");
    }

    // ------------------------------
    // 工具方法
    // ------------------------------

    /**
     * 验证枚举类型与数据库ENUM定义的一致性
     */
    private <T extends Enum<T>> void verifyEnumMapping(Class<T> enumClass, String query, String... expectedValues) {
        String columnType = jdbcTemplate.queryForObject(query, String.class);
        String enumValues = columnType.replaceAll("ENUM|\\(|\\)|'", "").trim();
        List<String> dbEnumValues = List.of(enumValues.split(","));

        List<String> codeEnumValues = List.of(enumClass.getEnumConstants()).stream()
            .map(Enum::name)
            .collect(Collectors.toList());

        assertThat(dbEnumValues).containsExactlyInAnyOrder(codeEnumValues.toArray(new String[0]));
        assertThat(dbEnumValues).containsExactlyInAnyOrder(expectedValues);
    }

    /**
     * 获取表的所有列名
     */
    private List<String> getTableColumns(String tableName) {
        return jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_NAME = ? ORDER BY ORDINAL_POSITION",
            String.class,
            tableName
        );
    }

    /**
     * 在Map列表中根据键值查找元素
     */
    private Map<String, Object> findByKey(List<Map<String, Object>> list, String key, String value) {
        return list.stream()
            .filter(map -> value.equals(map.get(key)))
            .findFirst()
            .orElse(null);
    }
}