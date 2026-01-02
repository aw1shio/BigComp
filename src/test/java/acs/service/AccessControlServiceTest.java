package acs.service;

import acs.domain.AccessRequest;
import acs.domain.AccessResult;
import acs.domain.Badge;
import acs.domain.BadgeStatus;
import acs.domain.AccessDecision;
import acs.domain.ReasonCode;
import acs.repository.AccessLogRepository;
import acs.repository.BadgeRepository;
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

    @Autowired
    private BadgeRepository badgeRepository;

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
        // 1. 提前创建并保存一个未绑定员工的徽章B998
        Badge unboundBadge = new Badge();
        unboundBadge.setBadgeId("B998");
        unboundBadge.setStatus(BadgeStatus.ACTIVE); // 确保徽章状态为活跃（避免触发BADGE_INACTIVE）
        unboundBadge.setEmployee(null); // 不绑定员工
        badgeRepository.save(unboundBadge); // 保存到数据库

        // 2. 使用该徽章发起访问请求
        AccessRequest request = new AccessRequest("B998", "R001", TEST_TIME);
        AccessResult result = accessControlService.processAccess(request);
        
        // 3. 验证结果
        assertEquals(AccessDecision.DENY, result.getDecision());
        assertEquals(ReasonCode.EMPLOYEE_NOT_FOUND, result.getReasonCode());

        // 4. 清理测试数据（可选，避免影响其他测试）
        badgeRepository.deleteById("B998");
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
        AccessRequest request = new AccessRequest("B001", "R002", TEST_TIME);
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