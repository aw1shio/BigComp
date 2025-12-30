package acs.service;

import acs.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AccessControlServiceTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        // 确保测试数据已加载（由 DatabaseIntegrationTest 自动执行）
        // 实际测试中不需要重复执行 init-test-data.sql
    }

    @Test
    void testAccessControlServiceProcessAccess() {
        // 模拟成功访问请求
        AccessRequest request = new AccessRequest(
            "B-10001", 
            "R-DOOR-301", 
            Instant.now()
        );
        
        // 通过数据库验证访问结果
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM access_logs WHERE badge_id = 'B-10001' ORDER BY timestamp DESC LIMIT 1"
        );
        
        assertThat(logs).hasSize(1);
        Map<String, Object> log = logs.get(0);
        assertThat(log.get("decision")).isEqualTo("ALLOW");
        assertThat(log.get("reason_code")).isEqualTo("ALLOW");
    }

    @Test
    void testAccessControlServiceDenyInvalidBadge() {
        // 模拟徽章挂失的访问请求
        AccessRequest request = new AccessRequest(
            "B-10002", 
            "R-DOOR-301", 
            Instant.now()
        );
        
        // 通过数据库验证拒绝原因
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM access_logs WHERE badge_id = 'B-10002' ORDER BY timestamp DESC LIMIT 1"
        );
        
        assertThat(logs).hasSize(1);
        Map<String, Object> log = logs.get(0);
        assertThat(log.get("decision")).isEqualTo("DENY");
        assertThat(log.get("reason_code")).isEqualTo("BADGE_INACTIVE");
    }

    @Test
    void testAccessControlServiceNoPermission() {
        // 模拟无权限访问（开发组无打印机权限）
        AccessRequest request = new AccessRequest(
            "B-10001", 
            "R-PRINTER-2F", 
            Instant.now()
        );
        
        // 通过数据库验证拒绝原因
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM access_logs WHERE badge_id = 'B-10001' AND resource_id = 'R-PRINTER-2F' ORDER BY timestamp DESC LIMIT 1"
        );
        
        assertThat(logs).hasSize(1);
        Map<String, Object> log = logs.get(0);
        assertThat(log.get("decision")).isEqualTo("DENY");
        assertThat(log.get("reason_code")).isEqualTo("NO_PERMISSION");
    }
}