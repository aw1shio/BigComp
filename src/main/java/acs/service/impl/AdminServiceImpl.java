package acs.service.impl;

import acs.domain.*;
import acs.repository.BadgeRepository;
import acs.repository.EmployeeRepository;
import acs.repository.GroupRepository;
import acs.repository.ResourceRepository;
import acs.service.AdminService;
import org.springframework.stereotype.Service;

/**
 * AdminServiceImpl：系统“管理端”业务实现
 *
 * 职责：
 * - 创建/维护 Employee、Badge、Group、Resource
 * - 建立 Employee↔Group 关系
 * - 建立 Group↔Resource 授权关系
 * - 修改 Badge 状态、Resource 状态
 *
 * 设计原则：
 * - 只做业务编排（orchestration）
 * - 所有数据存取通过 Repository 接口完成
 * - domain 层不写业务规则；repository 层不写业务规则
 *
 * 注意：
 * - 这里抛出 RuntimeException 是可以的（课程项目可接受），
 *   UI 层可以选择 catch 并给用户提示。
 * - AccessControlService.processAccess() 需要保证“不抛异常给 UI”，
 *   但 Admin 这类管理接口一般可以抛（或可以统一返回结果对象）。
 */

@Service
public class AdminServiceImpl implements AdminService {

    private final EmployeeRepository employeeRepo;
    private final BadgeRepository badgeRepo;
    private final GroupRepository groupRepo;
    private final ResourceRepository resourceRepo;

    public AdminServiceImpl(EmployeeRepository employeeRepo,
                            BadgeRepository badgeRepo,
                            GroupRepository groupRepo,
                            ResourceRepository resourceRepo) {
        this.employeeRepo = requireNonNull(employeeRepo, "employeeRepo");
        this.badgeRepo = requireNonNull(badgeRepo, "badgeRepo");
        this.groupRepo = requireNonNull(groupRepo, "groupRepo");
        this.resourceRepo = requireNonNull(resourceRepo, "resourceRepo");
    }

    // 员工管理
    @Override
    public void registerEmployee(String employeeId, String name) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(name, "name");

        if (employeeRepo.existsById(employeeId)) {
            throw new IllegalStateException("员工已存在：" + employeeId);
        }
        Employee employee = new Employee(employeeId, name);
        employeeRepo.save(employee);
    }

    // 徽章管理
    @Override
    public void issueBadge(String employeeId, String badgeId) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(badgeId, "badgeId");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("员工不存在：" + employeeId));
        if (badgeRepo.existsById(badgeId)) {
            throw new IllegalStateException("徽章已存在：" + badgeId);
        }

        // 解除员工原徽章绑定
        if (employee.getBadge() != null) {
            Badge oldBadge = employee.getBadge();
            oldBadge.setEmployee(null);
            badgeRepo.save(oldBadge);
        }

        // 创建新徽章并绑定
        Badge newBadge = new Badge(badgeId, BadgeStatus.ACTIVE);
        newBadge.setEmployee(employee);
        employee.setBadge(newBadge);

        badgeRepo.save(newBadge);
        employeeRepo.save(employee);
    }

    @Override
    public void setBadgeStatus(String badgeId, BadgeStatus status) {
        requireNonBlank(badgeId, "badgeId");
        if (status == null) {
            throw new IllegalArgumentException("状态不能为null");
        }

        Badge badge = badgeRepo.findById(badgeId)
                .orElseThrow(() -> new IllegalStateException("徽章不存在：" + badgeId));
        badge.setStatus(status);
        badgeRepo.save(badge);
    }

    // 权限组管理
    @Override
    public void createGroup(String groupId, String groupName) {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(groupName, "groupName");

        if (groupRepo.existsById(groupId)) {
            throw new IllegalStateException("组已存在：" + groupId);
        }
        Group group = new Group(groupId, groupName);
        groupRepo.save(group);
    }

    @Override
    public void assignEmployeeToGroup(String employeeId, String groupId) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(groupId, "groupId");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("员工不存在：" + employeeId));
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("组不存在：" + groupId));

        employee.getGroups().add(group);
        group.getEmployees().add(employee);

        employeeRepo.save(employee);
        groupRepo.save(group);
    }

    @Override
    public void removeEmployeeFromGroup(String employeeId, String groupId) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(groupId, "groupId");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("员工不存在：" + employeeId));
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("组不存在：" + groupId));

        employee.getGroups().remove(group);
        group.getEmployees().remove(employee);

        employeeRepo.save(employee);
        groupRepo.save(group);
    }

    // 资源管理
    @Override
    public void registerResource(String resourceId, String name, ResourceType type) {
        requireNonBlank(resourceId, "resourceId");
        requireNonBlank(name, "name");
        if (type == null) {
            throw new IllegalArgumentException("资源类型不能为null");
        }

        if (resourceRepo.existsById(resourceId)) {
            throw new IllegalStateException("资源已存在：" + resourceId);
        }
        Resource resource = new Resource(
                resourceId,
                name,
                type,
                ResourceState.AVAILABLE  // 默认初始状态为可用
        );
        resourceRepo.save(resource);
    }

    @Override
    public void setResourceState(String resourceId, ResourceState state) {
        requireNonBlank(resourceId, "resourceId");
        if (state == null) {
            throw new IllegalArgumentException("资源状态不能为null");
        }

        Resource resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("资源不存在：" + resourceId));
        resource.setResourceState(state);
        resourceRepo.save(resource);
    }

    // 权限管理
    @Override
    public void grantGroupAccessToResource(String groupId, String resourceId) {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(resourceId, "resourceId");

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("组不存在：" + groupId));
        Resource resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("资源不存在：" + resourceId));

        group.getResources().add(resource);
        resource.getGroups().add(group);

        groupRepo.save(group);
        resourceRepo.save(resource);
    }

    @Override
    public void revokeGroupAccessToResource(String groupId, String resourceId) {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(resourceId, "resourceId");

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("组不存在：" + groupId));
        Resource resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("资源不存在：" + resourceId));

        group.getResources().remove(resource);
        resource.getGroups().remove(group);

        groupRepo.save(group);
        resourceRepo.save(resource);
    }

    // 参数校验工具
    private static <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " 不能为null");
        }
        return obj;
    }

    private static String requireNonBlank(String s, String name) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(name + " 不能为空白");
        }
        return s;
    }
}