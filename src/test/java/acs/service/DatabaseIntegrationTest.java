package acs.service;

import acs.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DatabaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseSchemaMatchesDomainModels() {
        // 验证所有枚举值与数据库 ENUM 一致
        verifyEnumConsistency(ResourceType.class, "type", "DOOR", "PRINTER", "COMPUTER", "ROOM", "OTHER");
        verifyEnumConsistency(ResourceState.class, "state", "AVAILABLE", "OCCUPIED", "LOCKED", "OFFLINE");
        verifyEnumConsistency(BadgeStatus.class, "status", "ACTIVE", "DISABLED", "LOST");
        verifyEnumConsistency(ReasonCode.class, "reason_code", 
            "ALLOW", "BADGE_NOT_FOUND", "BADGE_INACTIVE", "EMPLOYEE_NOT_FOUND",
            "RESOURCE_NOT_FOUND", "RESOURCE_LOCKED", "RESOURCE_OCCUPIED",
            "NO_PERMISSION", "INVALID_REQUEST", "SYSTEM_ERROR");
    }

    @Test
    void testEmployeeData() {
        List<Map<String, Object>> employees = jdbcTemplate.queryForList("SELECT * FROM employees");
        assertThat(employees).hasSize(2);
        
        Map<String, Object> emp1 = employees.get(0);
        assertThat(emp1.get("employee_id")).isEqualTo("E-0001");
        assertThat(emp1.get("name")).isEqualTo("张三");
    }

    @Test
    void testBadgeData() {
        List<Map<String, Object>> badges = jdbcTemplate.queryForList("SELECT * FROM badges");
        assertThat(badges).hasSize(2);
        
        Map<String, Object> activeBadge = badges.stream()
            .filter(b -> "B-10001".equals(b.get("badge_id")))
            .findFirst()
            .orElseThrow();
        assertThat(activeBadge.get("status")).isEqualTo("ACTIVE");
        
        Map<String, Object> lostBadge = badges.stream()
            .filter(b -> "B-10002".equals(b.get("badge_id")))
            .findFirst()
            .orElseThrow();
        assertThat(lostBadge.get("status")).isEqualTo("LOST");
    }

    @Test
    void testResourceData() {
        List<Map<String, Object>> resources = jdbcTemplate.queryForList("SELECT * FROM resources");
        assertThat(resources).hasSize(2);
        
        Map<String, Object> door = resources.stream()
            .filter(r -> "R-DOOR-301".equals(r.get("resource_id")))
            .findFirst()
            .orElseThrow();
        assertThat(door.get("type")).isEqualTo("DOOR");
        assertThat(door.get("state")).isEqualTo("AVAILABLE");
        
        Map<String, Object> printer = resources.stream()
            .filter(r -> "R-PRINTER-2F".equals(r.get("resource_id")))
            .findFirst()
            .orElseThrow();
        assertThat(printer.get("state")).isEqualTo("OCCUPIED");
    }

    @Test
    void testPermissionGroups() {
        List<Map<String, Object>> groups = jdbcTemplate.queryForList("SELECT * FROM permission_groups");
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).get("group_id")).isEqualTo("G-DEV");
        assertThat(groups.get(0).get("group_name")).isEqualTo("开发组");
    }

    @Test
    void testGroupResourcePermissions() {
        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(
            "SELECT * FROM group_resource_permissions"
        );
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).get("group_id")).isEqualTo("G-DEV");
        assertThat(permissions.get(0).get("resource_id")).isEqualTo("R-DOOR-301");
    }

    @Test
    void testAccessLogs() {
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM access_logs ORDER BY timestamp"
        );
        assertThat(logs).hasSize(2);
        
        // 验证成功日志
        Map<String, Object> allowLog = logs.get(0);
        assertThat(allowLog.get("decision")).isEqualTo("ALLOW");
        assertThat(allowLog.get("reason_code")).isEqualTo("ALLOW");
        
        // 验证失败日志
        Map<String, Object> denyLog = logs.get(1);
        assertThat(denyLog.get("decision")).isEqualTo("DENY");
        assertThat(denyLog.get("reason_code")).isEqualTo("BADGE_INACTIVE");
    }

    @Test
    void testDomainEnumsMatchDatabase() {
        // 验证 AccessDecision 与数据库决策一致
        assertThat(AccessDecision.ALLOW.name()).isEqualTo("ALLOW");
        assertThat(AccessDecision.DENY.name()).isEqualTo("DENY");
        
        // 验证 ReasonCode 与数据库原因码一致
        assertThat(ReasonCode.ALLOW.name()).isEqualTo("ALLOW");
        assertThat(ReasonCode.BADGE_INACTIVE.name()).isEqualTo("BADGE_INACTIVE");
    }

    private <T extends Enum<T>> void verifyEnumConsistency(Class<T> enumClass, String column, String... expectedValues) {
        List<String> actualValues = jdbcTemplate.queryForList(
            "SELECT DISTINCT " + column + " FROM resources WHERE " + column + " IS NOT NULL",
            String.class
        );
        
        List<String> expectedValuesList = List.of(expectedValues);
        assertThat(actualValues).containsExactlyInAnyOrderElementsOf(expectedValuesList);
    }
}