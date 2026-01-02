package acs.service.impl;

import acs.domain.Badge;
import acs.domain.BadgeStatus;
import acs.domain.Employee;
import acs.domain.Group;
import acs.domain.Resource;
import acs.domain.ResourceState;
import acs.domain.ResourceType;
import acs.repository.BadgeRepository;
import acs.repository.EmployeeRepository;
import acs.repository.GroupRepository;
import acs.repository.ResourceRepository;
import acs.service.AdminService;

import java.util.Optional;

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

    // -------------------------
    // Employee & Badge
    // -------------------------

    /**
     * 注册员工：只创建员工基本信息
     * - 如果已存在：这里选择抛异常（也可以选择覆盖或忽略，需全组统一）
     */
    @Override
    public void registerEmployee(String employeeId, String name) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(name, "name");

        Optional<Employee> existing = employeeRepo.findById(employeeId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Employee already exists: " + employeeId);
        }

        Employee employee = new Employee(employeeId, name);
        employeeRepo.save(employee);
    }

    /**
     * 发放徽章：
     * - 员工必须存在
     * - badgeId 不能重复
     * - 建立 Employee↔Badge 双向关系（Employee.badgeId & Badge.employeeId）
     */
    @Override
    public void issueBadge(String employeeId, String badgeId) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(badgeId, "badgeId");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("Employee not found: " + employeeId));

        if (badgeRepo.findById(badgeId).isPresent()) {
            throw new IllegalStateException("Badge already exists: " + badgeId);
        }

        // 如果员工已有旧 badge：这里给一个明确策略（课程里必须“定规则”）
        // 策略：允许换卡，但旧卡不自动删除；旧卡应由管理员另行禁用/回收（更贴近真实系统）
        // 你也可以选择自动禁用旧卡，但要全组统一。
        String oldBadgeId = employee.getBadgeId();

        Badge newBadge = new Badge(badgeId);
        newBadge.setStatus(BadgeStatus.ACTIVE);
        newBadge.setEmployeeId(employeeId);

        employee.setBadgeId(badgeId);

        badgeRepo.save(newBadge);
        employeeRepo.save(employee);

        // 如果需要自动处理旧卡（可选），你可以开启下面逻辑（建议先不做，保持简单）
        // if (oldBadgeId != null && !oldBadgeId.isBlank()) {
        //     badgeRepo.findById(oldBadgeId).ifPresent(b -> {
        //         b.setStatus(BadgeStatus.DISABLED);
        //         badgeRepo.save(b);
        //     });
        // }
    }

    /**
     * 设置徽章状态：
     * - ACTIVE/DISABLED/LOST
     * - 徽章必须存在
     */
    @Override
    public void setBadgeStatus(String badgeId, BadgeStatus status) {
        requireNonBlank(badgeId, "badgeId");
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }

        Badge badge = badgeRepo.findById(badgeId)
                .orElseThrow(() -> new IllegalStateException("Badge not found: " + badgeId));

        badge.setStatus(status);
        badgeRepo.save(badge);
    }

    // -------------------------
    // Group membership
    // -------------------------

    /**
     * 创建权限组：
     * - groupId 不能重复
     */
    @Override
    public void createGroup(String groupId, String groupName) {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(groupName, "groupName");

        if (groupRepo.findById(groupId).isPresent()) {
            throw new IllegalStateException("Group already exists: " + groupId);
        }

        Group group = new Group(groupId, groupName);
        groupRepo.save(group);
    }

    /**
     * 员工加入组：
     * - 员工必须存在
     * - 组必须存在
     * - 关系存放在 Employee.groupIds（你们当前 domain 设计）
     */
    @Override
    public void assignEmployeeToGroup(String employeeId, String groupId) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(groupId, "groupId");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("Employee not found: " + employeeId));
        groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found: " + groupId));

        employee.addToGroup(groupId);
        employeeRepo.save(employee);
    }

    /**
     * 员工移除出组：
     * - 员工必须存在
     * - 组如果不存在：这里仍然允许移除（等价于“确保不在该组”）
     *   也可以选择严格抛异常，你们全组统一即可。
     */
    @Override
    public void removeEmployeeFromGroup(String employeeId, String groupId) {
        requireNonBlank(employeeId, "employeeId");
        requireNonBlank(groupId, "groupId");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("Employee not found: " + employeeId));

        employee.removeFromGroup(groupId);
        employeeRepo.save(employee);
    }

    // -------------------------
    // Resource & permissions
    // -------------------------

    /**
     * 注册资源：
     * - 资源必须唯一
     * - 默认状态建议为 AVAILABLE（Resource 构造器里已默认）
     */
    @Override
    public void registerResource(String resourceId, String name, ResourceType type) {
        requireNonBlank(resourceId, "resourceId");
        requireNonBlank(name, "name");
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        if (resourceRepo.findById(resourceId).isPresent()) {
            throw new IllegalStateException("Resource already exists: " + resourceId);
        }

        Resource resource = new Resource(resourceId, name, type);
        resourceRepo.save(resource);
    }

    /**
     * 设置资源状态：
     * - 资源必须存在
     */
    @Override
    public void setResourceState(String resourceId, ResourceState state) {
        requireNonBlank(resourceId, "resourceId");
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }

        Resource resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found: " + resourceId));

        resource.setState(state);
        resourceRepo.save(resource);
    }

    /**
     * 授权：让某个组可以访问某个资源
     *
     * 你们当前权限模型选择的是：Group 持有 resourceIds 集合
     * - group.resourceIds.add(resourceId)
     */
    @Override
    public void grantGroupAccessToResource(String groupId, String resourceId) {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(resourceId, "resourceId");

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found: " + groupId));
        resourceRepo.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found: " + resourceId));

        group.grantResource(resourceId);
        groupRepo.save(group);
    }

    /**
     * 撤销授权：让某个组不再能访问某个资源
     */
    @Override
    public void revokeGroupAccessToResource(String groupId, String resourceId) {
        requireNonBlank(groupId, "groupId");
        requireNonBlank(resourceId, "resourceId");

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found: " + groupId));

        group.revokeResource(resourceId);
        groupRepo.save(group);
    }

    // -------------------------
    // Helpers（参数校验工具）
    // -------------------------

    private static <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        return obj;
    }

    private static String requireNonBlank(String s, String name) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return s;
    }
}
