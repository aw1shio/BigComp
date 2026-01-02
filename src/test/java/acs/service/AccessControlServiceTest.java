package acs.service;

import acs.domain.AccessRequest;
import acs.domain.AccessResult;
import acs.domain.AccessDecision;
import acs.domain.ReasonCode;
import acs.repository.AccessLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.temporal.ChronoUnit;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccessControlServiceTest {

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private AccessLogRepository logRepository;

    // 测试时间（使用固定时间便于测试）
    private final Instant TEST_TIME = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    @Test
    void testInvalidRequest_MissingParams() {
        // 测试参数为空的无效请求
        AccessRequest invalidRequest = new AccessRequest("", "R001", TEST_TIME);
        AccessResult result = accessControlService.processAccess(invalidRequest);
        
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.INVALID_REQUEST, result.getReasonCode());
    }

    @Test
    void testBadgeNotFound() {
        // 测试不存在的徽章
        AccessRequest request = new AccessRequest("B999", "R001", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.BADGE_NOT_FOUND, result.getReasonCode());
    }

    @Test
    void testBadgeInactive() {
        // 测试已禁用的徽章（B003状态为DISABLED）
        AccessRequest request = new AccessRequest("B003", "R004", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.BADGE_INACTIVE, result.getReasonCode());
    }

    @Test
    void testEmployeeNotFound() {
        // 假设创建一个未绑定员工的徽章（测试数据中无此徽章，需提前创建）
        // 此处简化：可通过AdminService创建一个无员工的徽章B998
        AccessRequest request = new AccessRequest("B998", "R001", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.EMPLOYEE_NOT_FOUND, result.getReasonCode());
    }

    @Test
    void testResourceNotFound() {
        // 测试不存在的资源
        AccessRequest request = new AccessRequest("B001", "R999", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.RESOURCE_NOT_FOUND, result.getReasonCode());
    }

    @Test
    void testResourceLocked() {
        // 测试访问锁定的资源（R002状态为LOCKED）
        AccessRequest request = new AccessRequest("B004", "R002", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.RESOURCE_LOCKED, result.getReasonCode());
    }

    @Test
    void testNoPermission() {
        // 测试无权限访问（访客E007访问工程车间R004）
        AccessRequest request = new AccessRequest("B007", "R004", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.NO_PERMISSION, result.getReasonCode());
    }

    @Test
    void testAccessAllowed() {
        // 测试有权限访问（管理员E001访问主入口R001）
        AccessRequest request = new AccessRequest("B001", "R001", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        assertEquals(AccessDecision.ALLOW, result.getDecision());
        assertEquals(ReasonCode.ALLOW, result.getReasonCode());
    }

    @Test
    void testLogRecordedAfterAccess() {
        // 测试访问后日志是否被正确记录
        long initialCount = logRepository.count();
        AccessRequest request = new AccessRequest("B001", "R001", TEST_TIME);
        accessControlService.processAccess(request);
        
        assertEquals(initialCount + 1, logRepository.count());
    }
}