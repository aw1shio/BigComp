package acs.service;

import acs.domain.BadgeStatus;
import acs.domain.Group;
import acs.domain.Employee;
import acs.domain.Resource;
import acs.domain.ResourceType;
import acs.domain.ResourceState;
import acs.repository.BadgeRepository;
import acs.repository.EmployeeRepository;
import acs.repository.GroupRepository;
import acs.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional; 

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void testRegisterEmployee() {
        // 测试注册新员工
        String empId = "E999";
        String name = "Test Employee";
        adminService.registerEmployee(empId, name);
        
        Employee employee = employeeRepository.findById(empId).orElseThrow();
        assertEquals(name, employee.getEmployeeName());
    }

    @Test
    void testRegisterDuplicateEmployee() {
        // 测试注册重复员工（应抛出异常）
        assertThrows(IllegalStateException.class, () -> {
            adminService.registerEmployee("E001", "Duplicate Name");
        });
    }

    @Test
    void testIssueBadge() {
        // 测试发放徽章给员工
        String empId = "E007";
        String badgeId = "B999";
        adminService.issueBadge(empId, badgeId);
        
        // 验证徽章与员工绑定
        var badge = badgeRepository.findById(badgeId).orElseThrow();
        assertEquals(BadgeStatus.ACTIVE, badge.getStatus());
        assertEquals(empId, badge.getEmployee().getEmployeeId());
    }

    @Test
    void testSetBadgeStatus() {
        // 测试修改徽章状态
        String badgeId = "B002";
        adminService.setBadgeStatus(badgeId, BadgeStatus.LOST);
        
        var badge = badgeRepository.findById(badgeId).orElseThrow();
        assertEquals(BadgeStatus.LOST, badge.getStatus());
    }

    @Test
    void testCreateGroup() {
        // 测试创建新组
        String groupId = "G999";
        String groupName = "Test Group";
        adminService.createGroup(groupId, groupName);
        
        Group group = groupRepository.findById(groupId).orElseThrow();
        assertEquals(groupName, group.getName());
    }

    @Test
    @Transactional  // 添加此注解
    void testAssignEmployeeToGroup() {
        // 测试将员工加入组
        String empId = "E007";
        String groupId = "G002";
        adminService.assignEmployeeToGroup(empId, groupId);
        
        Employee employee = employeeRepository.findById(empId).orElseThrow();
        assertTrue(employee.getGroups().stream()
                .anyMatch(g -> g.getGroupId().equals(groupId)));
    }

    @Test
    void testRegisterResource() {
        // 测试注册新资源
        String resId = "R999";
        String resName = "Test Printer";
        adminService.registerResource(resId, resName, ResourceType.PRINTER);
        
        Resource resource = resourceRepository.findById(resId).orElseThrow();
        assertEquals(resName, resource.getResourceName());
        assertEquals(ResourceState.AVAILABLE, resource.getResourceState());
    }

    @Test
    @Transactional
    void testGrantGroupAccessToResource() {
        // 测试授权组访问资源
        String groupId = "G005";
        String resId = "R003";
        adminService.grantGroupAccessToResource(groupId, resId);
        
        Group group = groupRepository.findById(groupId).orElseThrow();
        assertTrue(group.getResources().stream()
                .anyMatch(r -> r.getResourceId().equals(resId)));
    }
}