package acs.service.impl;

import acs.domain.AccessDecision;
import acs.domain.AccessRequest;
import acs.domain.AccessResult;
import acs.domain.Badge;
import acs.domain.BadgeStatus;
import acs.domain.Employee;
import acs.domain.Group;
import acs.domain.LogEntry;
import acs.domain.ReasonCode;
import acs.domain.Resource;
import acs.domain.ResourceState;
import acs.log.LogService;
import acs.repository.BadgeRepository;
import acs.repository.EmployeeRepository;
import acs.repository.GroupRepository;
import acs.repository.ResourceRepository;
import acs.service.AccessControlService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    private final BadgeRepository badgeRepository;
    private final EmployeeRepository employeeRepository;
    private final ResourceRepository resourceRepository;
    private final GroupRepository groupRepository;
    private final LogService logService;

    // 构造器注入所有依赖
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
        // 初始化日志基础信息
        String badgeId = request.getBadgeId();
        String resourceId = request.getResourceId();
        String employeeId = null;
        AccessDecision decision = AccessDecision.DENY;
        ReasonCode reasonCode = ReasonCode.SYSTEM_ERROR;

        try {
            // 1. 校验请求参数合法性
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
            employeeId = badge.getEmployeeId(); // 记录员工ID（可能为null）

            // 3. 检查徽章状态是否活跃
            if (badge.getStatus() != BadgeStatus.ACTIVE) {
                reasonCode = ReasonCode.BADGE_INACTIVE;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "徽章不可用（已禁用或挂失）");
            }

            // 4. 检查徽章绑定的员工是否存在
            if (employeeId == null || employeeId.isBlank()) {
                reasonCode = ReasonCode.EMPLOYEE_NOT_FOUND;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "徽章未绑定员工");
            }
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                reasonCode = ReasonCode.EMPLOYEE_NOT_FOUND;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "员工信息不存在");
            }
            Employee employee = employeeOpt.get();

            // 5. 检查资源是否存在
            Optional<Resource> resourceOpt = resourceRepository.findById(resourceId);
            if (resourceOpt.isEmpty()) {
                reasonCode = ReasonCode.RESOURCE_NOT_FOUND;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源不存在");
            }
            Resource resource = resourceOpt.get();

            // 6. 检查资源状态是否允许访问
            ResourceState resourceState = resource.getState();
            if (resourceState == ResourceState.LOCKED) {
                reasonCode = ReasonCode.RESOURCE_LOCKED;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源已锁定");
            }
            if (resourceState == ResourceState.OCCUPIED) {
                reasonCode = ReasonCode.RESOURCE_OCCUPIED;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源当前被占用");
            }
            if (resourceState == ResourceState.OFFLINE) {
                reasonCode = ReasonCode.RESOURCE_OCCUPIED; // 离线状态视为不可访问
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源离线不可用");
            }
            
            if (!resourceRepository.tryOccupy(resourceId)) {
                reasonCode = ReasonCode.RESOURCE_OCCUPIED;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "资源当前被占用");
            }
            
            // 7. 检查员工所属组是否有资源访问权限
            Set<String> groupIds = employee.getGroupIds();
            boolean hasPermission = false;
            for (String groupId : groupIds) {
                Optional<Group> groupOpt = groupRepository.findById(groupId);
                if (groupOpt.isPresent() && groupOpt.get().getResourceIds().contains(resourceId)) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                reasonCode = ReasonCode.NO_PERMISSION;
                return buildResultAndLog(request, decision, reasonCode, employeeId, "无访问权限");
            }

            // 8. 所有检查通过，允许访问
            decision = AccessDecision.ALLOW;
            reasonCode = ReasonCode.ALLOW;
            return buildResultAndLog(request, decision, reasonCode, employeeId, "访问允许");

        } catch (Exception e) {
            // 捕获所有未预期异常，转化为系统错误
            reasonCode = ReasonCode.SYSTEM_ERROR;
            return buildResultAndLog(request, decision, reasonCode, employeeId, "系统内部错误：" + e.getMessage());
        }
    }

    /**
     * 异步入口：用于并发模拟/多线程访问。
     *
     * 注意：这里不要直接在同一个类里用内部方法自调用 @Async 方法，否则 Spring 代理不会生效。
     * 只要 UI/Controller/测试通过 Spring 容器注入的 bean 调用本方法即可触发异步执行。
     */
    @Override
    @Async("acsExecutor")
    public CompletableFuture<AccessResult> processAccessAsync(AccessRequest request) {
        return CompletableFuture.completedFuture(processAccess(request));
    }

    /**
     * 构建访问结果并记录日志
     */
    private AccessResult buildResultAndLog(AccessRequest request,
                                           AccessDecision decision,
                                           ReasonCode reasonCode,
                                           String employeeId,
                                           String message) {
        // 记录日志
        LogEntry logEntry = new LogEntry(
                request.getTimestamp(),
                request.getBadgeId(),
                employeeId,
                request.getResourceId(),
                decision,
                reasonCode
        );
        logService.record(logEntry);

        // 返回访问结果
        return new AccessResult(decision, reasonCode, message);
    }
}