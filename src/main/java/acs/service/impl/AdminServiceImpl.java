package acs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import acs.cache.LocalCacheManager;
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

@Service
public class AdminServiceImpl implements AdminService {

    // 注入LocalCacheManager
    private final LocalCacheManager cacheManager;
    private final EmployeeRepository employeeRepository;
    private final BadgeRepository badgeRepository;
    private final GroupRepository groupRepository;
    private final ResourceRepository resourceRepository;

    public AdminServiceImpl(EmployeeRepository employeeRepository,
                            BadgeRepository badgeRepository,
                            GroupRepository groupRepository,
                            ResourceRepository resourceRepository,
                            LocalCacheManager cacheManager) {
        this.employeeRepository = employeeRepository;
        this.badgeRepository = badgeRepository;
        this.groupRepository = groupRepository;
        this.resourceRepository = resourceRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional
    public void registerEmployee(String employeeId, String name) {
        if (employeeRepository.existsById(employeeId)) {
            throw new IllegalStateException("员工ID已存在: " + employeeId);
        }
        Employee employee = new Employee(employeeId, name);
        employeeRepository.save(employee);
        // 同步缓存
        cacheManager.updateEmployee(employee);
    }

    @Override
    @Transactional
    public void issueBadge(String employeeId, String badgeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在: " + employeeId));
        
        if (badgeRepository.existsById(badgeId)) {
            throw new IllegalStateException("徽章ID已存在: " + badgeId);
        }
        
        Badge badge = new Badge(badgeId, BadgeStatus.ACTIVE);
        badge.setEmployee(employee);
        badge = badgeRepository.saveAndFlush(badge);
        
        employee.setBadge(badge);
        employeeRepository.save(employee);
        // 同步缓存
        cacheManager.updateBadge(badge);
        cacheManager.updateEmployee(employee);
    }

    @Override
    @Transactional
    public void setBadgeStatus(String badgeId, BadgeStatus status) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("徽章不存在: " + badgeId));
        badge.setStatus(status);
        badgeRepository.save(badge);
        // 同步缓存
        cacheManager.updateBadge(badge);
    }

    @Override
    @Transactional
    public void createGroup(String groupId, String groupName) {
        if (groupRepository.existsById(groupId)) {
            throw new IllegalStateException("组ID已存在: " + groupId);
        }
        Group group = new Group(groupId, groupName);
        groupRepository.save(group);
        // 同步缓存
        cacheManager.updateGroup(group);
    }

    @Override
    @Transactional
    public void assignEmployeeToGroup(String employeeId, String groupId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在: " + employeeId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("组不存在: " + groupId));
        
        employee.getGroups().add(group);
        group.getEmployees().add(employee);
        
        employeeRepository.save(employee);
        groupRepository.save(group);
        // 同步缓存
        cacheManager.updateEmployee(employee);
        cacheManager.updateGroup(group);
    }

    @Override
    @Transactional
    public void removeEmployeeFromGroup(String employeeId, String groupId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在: " + employeeId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("组不存在: " + groupId));
        
        employee.getGroups().remove(group);
        group.getEmployees().remove(employee);
        
        employeeRepository.save(employee);
        groupRepository.save(group);
        // 同步缓存
        cacheManager.updateEmployee(employee);
        cacheManager.updateGroup(group);
    }

    @Override
    @Transactional
    public void registerResource(String resourceId, String name, ResourceType type) {
        if (resourceRepository.existsById(resourceId)) {
            throw new IllegalStateException("资源ID已存在: " + resourceId);
        }
        // 新资源默认状态为可用
        Resource resource = new Resource(resourceId, name, type, ResourceState.AVAILABLE);
        resourceRepository.save(resource);
        // 同步缓存
        cacheManager.updateResource(resource);
    }

    @Override
    @Transactional
    public void setResourceState(String resourceId, ResourceState state) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("资源不存在: " + resourceId));
        resource.setResourceState(state);
        resourceRepository.save(resource);
        // 同步缓存
        cacheManager.updateResource(resource);
    }

    @Override
    @Transactional
    public void grantGroupAccessToResource(String groupId, String resourceId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("组不存在: " + groupId));
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("资源不存在: " + resourceId));
        
        group.getResources().add(resource);
        resource.getGroups().add(group);
        
        groupRepository.save(group);
        resourceRepository.save(resource);
        // 同步缓存
        cacheManager.updateGroup(group);
        cacheManager.updateResource(resource);
    }

    @Override
    @Transactional
    public void revokeGroupAccessToResource(String groupId, String resourceId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("组不存在: " + groupId));
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("资源不存在: " + resourceId));
        
        group.getResources().remove(resource);
        resource.getGroups().remove(group);
        
        groupRepository.save(group);
        resourceRepository.save(resource);
        // 同步缓存
        cacheManager.updateGroup(group);
        cacheManager.updateResource(resource);
    }
}