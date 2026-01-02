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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    private final EmployeeRepository employeeRepository;
    private final BadgeRepository badgeRepository;
    private final GroupRepository groupRepository;
    private final ResourceRepository resourceRepository;

    public AdminServiceImpl(EmployeeRepository employeeRepository,
                            BadgeRepository badgeRepository,
                            GroupRepository groupRepository,
                            ResourceRepository resourceRepository) {
        this.employeeRepository = employeeRepository;
        this.badgeRepository = badgeRepository;
        this.groupRepository = groupRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    @Transactional
    public void registerEmployee(String employeeId, String name) {
        if (employeeRepository.existsById(employeeId)) {
            throw new IllegalStateException("员工ID已存在: " + employeeId);
        }
        employeeRepository.save(new Employee(employeeId, name));
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
    }

    @Override
    @Transactional
    public void setBadgeStatus(String badgeId, BadgeStatus status) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("徽章不存在: " + badgeId));
        badge.setStatus(status);
        badgeRepository.save(badge);
    }

    @Override
    @Transactional
    public void createGroup(String groupId, String groupName) {
        if (groupRepository.existsById(groupId)) {
            throw new IllegalStateException("组ID已存在: " + groupId);
        }
        groupRepository.save(new Group(groupId, groupName));
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
    }

    @Override
    @Transactional
    public void registerResource(String resourceId, String name, ResourceType type) {
        if (resourceRepository.existsById(resourceId)) {
            throw new IllegalStateException("资源ID已存在: " + resourceId);
        }
        // 新资源默认状态为可用
        resourceRepository.save(new Resource(resourceId, name, type, ResourceState.AVAILABLE));
    }

    @Override
    @Transactional
    public void setResourceState(String resourceId, ResourceState state) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("资源不存在: " + resourceId));
        resource.setResourceState(state);
        resourceRepository.save(resource);
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
    }
}