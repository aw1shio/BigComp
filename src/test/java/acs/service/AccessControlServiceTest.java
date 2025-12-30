package acs.service;

import acs.domain.*;
import acs.log.LogService;
import acs.repository.BadgeRepository;
import acs.repository.EmployeeRepository;
import acs.repository.GroupRepository;
import acs.repository.ResourceRepository;
import acs.service.impl.AccessControlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessControlServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private AccessControlServiceImpl accessControlService;

    private AccessRequest validRequest;
    private Instant testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = Instant.now();
        validRequest = new AccessRequest("B-10001", "R-DOOR-301", testTimestamp);
    }

    // 测试场景1：所有条件都满足，允许访问
    @Test
    void processAccess_ValidRequest_ShouldAllow() {
        // 准备测试数据
        Badge badge = new Badge("B-10001");
        badge.setStatus(BadgeStatus.ACTIVE);
        badge.setEmployeeId("E-0001");

        Employee employee = new Employee("E-0001", "张三");
        employee.addToGroup("G-DEV");

        Resource resource = new Resource("R-DOOR-301", "301室门", ResourceType.DOOR);
        resource.setState(ResourceState.AVAILABLE);

        Group group = new Group("G-DEV", "开发组", Set.of("R-DOOR-301"));

        // 模拟依赖行为
        when(badgeRepository.findById("B-10001")).thenReturn(Optional.of(badge));
        when(employeeRepository.findById("E-0001")).thenReturn(Optional.of(employee));
        when(resourceRepository.findById("R-DOOR-301")).thenReturn(Optional.of(resource));
        when(groupRepository.findById("G-DEV")).thenReturn(Optional.of(group));

        // 执行测试
        AccessResult result = accessControlService.processAccess(validRequest);

        // 验证结果
        assertThat(result.getDecision()).isEqualTo(AccessDecision.ALLOW);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.ALLOW);
        assertThat(result.getMessage()).contains("访问允许");

        // 验证日志被记录
        verify(logService, times(1)).record(any(LogEntry.class));
    }

    // 测试场景2：徽章不存在
    @Test
    void processAccess_BadgeNotFound_ShouldDeny() {
        when(badgeRepository.findById("B-10001")).thenReturn(Optional.empty());

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.BADGE_NOT_FOUND);
        verify(logService, times(1)).record(any(LogEntry.class));
    }

    // 测试场景3：徽章未激活（挂失状态）
    @Test
    void processAccess_BadgeInactive_ShouldDeny() {
        Badge badge = new Badge("B-10001");
        badge.setStatus(BadgeStatus.LOST); // 挂失状态
        badge.setEmployeeId("E-0001");

        when(badgeRepository.findById("B-10001")).thenReturn(Optional.of(badge));

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.BADGE_INACTIVE);
    }

    // 测试场景4：徽章未绑定员工
    @Test
    void processAccess_BadgeNoEmployee_ShouldDeny() {
        Badge badge = new Badge("B-10001");
        badge.setStatus(BadgeStatus.ACTIVE);
        badge.setEmployeeId(null); // 未绑定员工

        when(badgeRepository.findById("B-10001")).thenReturn(Optional.of(badge));

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.EMPLOYEE_NOT_FOUND);
    }

    // 测试场景5：员工不存在
    @Test
    void processAccess_EmployeeNotFound_ShouldDeny() {
        Badge badge = new Badge("B-10001");
        badge.setStatus(BadgeStatus.ACTIVE);
        badge.setEmployeeId("E-9999"); // 不存在的员工ID

        when(badgeRepository.findById("B-10001")).thenReturn(Optional.of(badge));
        when(employeeRepository.findById("E-9999")).thenReturn(Optional.empty());

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.EMPLOYEE_NOT_FOUND);
    }

    // 测试场景6：资源不存在
    @Test
    void processAccess_ResourceNotFound_ShouldDeny() {
        Badge badge = new Badge("B-10001");
        badge.setStatus(BadgeStatus.ACTIVE);
        badge.setEmployeeId("E-0001");

        Employee employee = new Employee("E-0001", "张三");

        when(badgeRepository.findById("B-10001")).thenReturn(Optional.of(badge));
        when(employeeRepository.findById("E-0001")).thenReturn(Optional.of(employee));
        when(resourceRepository.findById("R-DOOR-301")).thenReturn(Optional.empty());

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.RESOURCE_NOT_FOUND);
    }

    // 测试场景7：资源被锁定
    @Test
    void processAccess_ResourceLocked_ShouldDeny() {
        Badge badge = new Badge("B-10001");
        badge.setStatus(BadgeStatus.ACTIVE);
        badge.setEmployeeId("E-0001");

        Employee employee = new Employee("E-0001", "张三");

        Resource resource = new Resource("R-DOOR-301", "301室门", ResourceType.DOOR);
        resource.setState(ResourceState.LOCKED); // 资源锁定

        when(badgeRepository.findById("B-10001")).thenReturn(Optional.of(badge));
        when(employeeRepository.findById("E-0001")).thenReturn(Optional.of(employee));
        when(resourceRepository.findById("R-DOOR-301")).thenReturn(Optional.of(resource));

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.RESOURCE_LOCKED);
    }

    // 测试场景8：无访问权限
    @Test
    void processAccess_NoPermission_ShouldDeny() {
        Badge badge = new Badge("B-10001");
        badge.setStatus(BadgeStatus.ACTIVE);
        badge.setEmployeeId("E-0001");

        Employee employee = new Employee("E-0001", "张三");
        employee.addToGroup("G-DEV"); // 属于开发组

        Resource resource = new Resource("R-DOOR-301", "301室门", ResourceType.DOOR);
        resource.setState(ResourceState.AVAILABLE);

        Group group = new Group("G-DEV", "开发组"); // 开发组没有该资源权限

        when(badgeRepository.findById("B-10001")).thenReturn(Optional.of(badge));
        when(employeeRepository.findById("E-0001")).thenReturn(Optional.of(employee));
        when(resourceRepository.findById("R-DOOR-301")).thenReturn(Optional.of(resource));
        when(groupRepository.findById("G-DEV")).thenReturn(Optional.of(group));

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.NO_PERMISSION);
    }

    // 测试场景9：无效请求参数（徽章ID为空）
    @Test
    void processAccess_InvalidRequest_ShouldDeny() {
        AccessRequest invalidRequest = new AccessRequest("", "R-DOOR-301", testTimestamp);

        AccessResult result = accessControlService.processAccess(invalidRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.INVALID_REQUEST);
    }

    // 测试场景10：系统异常处理
    @Test
    void processAccess_SystemError_ShouldDeny() {
        when(badgeRepository.findById(any())).thenThrow(new RuntimeException("数据库连接失败"));

        AccessResult result = accessControlService.processAccess(validRequest);

        assertThat(result.getDecision()).isEqualTo(AccessDecision.DENY);
        assertThat(result.getReasonCode()).isEqualTo(ReasonCode.SYSTEM_ERROR);
        assertThat(result.getMessage()).contains("系统内部错误");
        verify(logService, times(1)).record(any(LogEntry.class));
    }
}