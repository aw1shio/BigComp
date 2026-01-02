package acs.service;

import acs.domain.LogEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LogQueryServiceTest {

    @Autowired
    private LogQueryService logQueryService;

    // 测试时间范围（覆盖测试数据中的日志）
    private final Instant START = LocalDateTime.of(2024, 5, 1, 8, 0)
            .atZone(ZoneId.systemDefault()).toInstant();
    private final Instant END = LocalDateTime.of(2024, 5, 1, 18, 0)
            .atZone(ZoneId.systemDefault()).toInstant();

    @Test
    void testFindByBadge() {
        // 测试按徽章查询日志
        List<LogEntry> logs = logQueryService.findByBadge("B001", START, END);
        
        assertFalse(logs.isEmpty());
        logs.forEach(log -> assertEquals("B001", log.getBadge().getBadgeId()));
    }

    @Test
    void testFindByEmployee() {
        // 测试按员工查询日志
        List<LogEntry> logs = logQueryService.findByEmployee("E002", START, END);
        
        assertFalse(logs.isEmpty());
        logs.forEach(log -> assertEquals("E002", log.getEmployee().getEmployeeId()));
    }

    @Test
    void testFindByResource() {
        // 测试按资源查询日志
        List<LogEntry> logs = logQueryService.findByResource("R001", START, END);
        
        assertFalse(logs.isEmpty());
        logs.forEach(log -> assertEquals("R001", log.getResource().getResourceId()));
    }

    @Test
    void testFindDenied() {
        // 测试查询被拒绝的日志
        List<LogEntry> logs = logQueryService.findDenied(START, END);
        
        assertFalse(logs.isEmpty());
        logs.forEach(log -> assertEquals("DENY", log.getDecision().name()));
    }
}