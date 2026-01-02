package acs.service.impl;

import acs.domain.*;
import acs.log.LogService;
import acs.repository.BadgeRepository;
import acs.repository.EmployeeRepository;
import acs.repository.GroupRepository;
import acs.repository.ResourceRepository;
import acs.service.AccessControlService;
import org.springframework.stereotype.Service;

// 补充日期时间类导入（解决无法识别的问题）
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.Optional;
import java.util.Set;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    private final BadgeRepository badgeRepository;
    private final EmployeeRepository employeeRepository;
    private final ResourceRepository resourceRepository;
    private final GroupRepository groupRepository;
    private final LogService logService;

    public AccessControlServiceImpl(BadgeRepository badgeRepository,
                                    EmployeeRepository employeeRepository,
                                    ResourceRepository resourceRepository,
                                    GroupRepository groupRepository,
                                    LogService logService) {
        this.badgeRepository = badgeRepository;
        this.employeeRepository = employeeRepository;
        this.resourceRepository = resourceRepository;
        this.groupRepository = groupRepository;
        this.logService = logService;
    }

    @Override
    public AccessResult processAccess(AccessRequest request) {
        String badgeId = request.getBadgeId();
        String resourceId = request.getResourceId();
        String employeeId = null;
        AccessDecision decision = AccessDecision.DENY;
        ReasonCode reasonCode = ReasonCode.SYSTEM_ERROR;

        try {
            // 1. 校验请求参数
            if (badgeId == null || badgeId.isBlank() || 
                resourceId == null || resourceId.isBlank() || 
                request.getTimestamp() == null) {
                reasonCode = ReasonCode.INVALID_REQUEST;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "无效请求：参数缺失或为空");
            }

            // 2. 检查徽章是否存在
            Optional<Badge> badgeOpt = badgeRepository.findById(badgeId);
            if (badgeOpt.isEmpty()) {
                reasonCode = ReasonCode.BADGE_NOT_FOUND;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "徽章不存在");
            }
            Badge badge = badgeOpt.get();
            employeeId = badge.getEmployee() != null ? badge.getEmployee().getEmployeeId() : null;

            // 3. 检查徽章状态
            if (badge.getStatus() != BadgeStatus.ACTIVE) {
                reasonCode = ReasonCode.BADGE_INACTIVE;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "徽章不可用（已禁用或挂失）");
            }

            // 4. 检查员工是否存在
            if (badge.getEmployee() == null) {
                reasonCode = ReasonCode.EMPLOYEE_NOT_FOUND;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "徽章未绑定员工");
            }
            Employee employee = badge.getEmployee();
            Optional<Employee> employeeOpt = employeeRepository.findById(employee.getEmployeeId());
            if (employeeOpt.isEmpty()) {
                reasonCode = ReasonCode.EMPLOYEE_NOT_FOUND;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "员工信息不存在");
            }

            // 5. 检查资源是否存在
            Optional<Resource> resourceOpt = resourceRepository.findById(resourceId);
            if (resourceOpt.isEmpty()) {
                reasonCode = ReasonCode.RESOURCE_NOT_FOUND;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源不存在");
            }
            Resource resource = resourceOpt.get();

            // 6. 检查资源状态
            ResourceState resourceState = resource.getResourceState();
            if (resourceState == ResourceState.LOCKED) {
                reasonCode = ReasonCode.RESOURCE_LOCKED;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源已锁定");
            }
            if (resourceState == ResourceState.OCCUPIED || resourceState == ResourceState.OFFLINE) {
                reasonCode = ReasonCode.RESOURCE_OCCUPIED;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源不可用（被占用或离线）");
            }

            // 7. 检查员工所属组的权限
            Set<Group> employeeGroups = employee.getGroups();
            boolean hasPermission = employeeGroups.stream()
                    .flatMap(group -> group.getResources().stream())
                    .anyMatch(r -> r.getResourceId().equals(resourceId));

            if (!hasPermission) {
                reasonCode = ReasonCode.NO_PERMISSION;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "无访问权限");
            }

            // 8. 所有检查通过
            decision = AccessDecision.ALLOW;
            reasonCode = ReasonCode.ALLOW;
            return buildResultAndLog(request, decision, reasonCode, employeeId, "访问允许");

        } catch (Exception e) {
            reasonCode = ReasonCode.SYSTEM_ERROR;
            return buildResultAndLog(request, decision, reasonCode, employeeId, "系统内部错误：" + e.getMessage());
        }
    }

    private AccessResult buildResultAndLog(AccessRequest request,
                                           AccessDecision decision,
                                           ReasonCode reasonCode,
                                           String employeeId,
                                           String message) {
        // 转换时间类型：Instant -> LocalDateTime
        LocalDateTime logTime = request.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // 构建日志实体（关联实体对象）
        LogEntry logEntry = new LogEntry();
        logEntry.setTimestamp(logTime);
        logEntry.setBadge(badgeRepository.findById(request.getBadgeId()).orElse(null));
        logEntry.setEmployee(employeeId != null ? employeeRepository.findById(employeeId).orElse(null) : null);
        logEntry.setResource(resourceRepository.findById(request.getResourceId()).orElse(null));
        logEntry.setDecision(decision);
        logEntry.setReasonCode(reasonCode);

        logService.record(logEntry);

        return new AccessResult(decision, reasonCode, message);
    }
}