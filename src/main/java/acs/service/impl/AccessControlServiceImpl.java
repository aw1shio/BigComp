package acs.service.impl;

import acs.domain.AccessRequest;
import acs.domain.AccessResult;
import acs.cache.LocalCacheManager;
import acs.domain.AccessDecision;
import acs.domain.Badge;
import acs.domain.BadgeStatus;
import acs.domain.Employee;
import acs.domain.LogEntry;
import acs.domain.ReasonCode;
import acs.domain.Resource;
import acs.domain.ResourceState;
import acs.log.LogService;
import acs.service.AccessControlService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    private final LogService logService;
    // 在类中注入LocalCacheManager
    private final LocalCacheManager cacheManager;

    public AccessControlServiceImpl(
                                LogService logService,
                                LocalCacheManager cacheManager) {
        this.logService = logService;
        this.cacheManager = cacheManager;
    }

    // 修改processAccess方法中的数据访问部分，使用缓存
    @Override
    @Transactional
    public AccessResult processAccess(AccessRequest request) {
        // 1. 验证请求参数
        if (request.getBadgeId() == null || request.getBadgeId().trim().isEmpty() ||
                request.getResourceId() == null || request.getResourceId().trim().isEmpty() ||
                request.getTimestamp() == null) {
            AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.INVALID_REQUEST, "无效的访问请求参数");
            recordLog(null, null, null, result, request);
            return result;
        }

        try {
            // 2. 验证徽章存在性 - 从缓存获取
            Badge badge = cacheManager.getBadge(request.getBadgeId());
            if (badge == null) {
                AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.BADGE_NOT_FOUND, "徽章不存在");
                recordLog(null, null, null, result, request);
                return result;
            }

            // 3. 验证徽章状态
            if (badge.getStatus() != BadgeStatus.ACTIVE) {
                AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.BADGE_INACTIVE, "徽章不可用（已禁用或挂失）");
                recordLog(badge, null, null, result, request);
                return result;
            }

            // 4. 验证员工存在性 - 从缓存获取
            Employee employee = badge.getEmployee() != null ? 
                cacheManager.getEmployee(badge.getEmployee().getEmployeeId()) : null;
            if (employee == null) {
                AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.EMPLOYEE_NOT_FOUND, "徽章未绑定有效员工");
                recordLog(badge, null, null, result, request);
                return result;
            }

            // 5. 验证资源存在性 - 从缓存获取
            Resource resource = cacheManager.getResource(request.getResourceId());
            if (resource == null) {
                AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.RESOURCE_NOT_FOUND, "访问的资源不存在");
                recordLog(badge, employee, null, result, request);
                return result;
            }

            // 6. 验证权限（员工所属组是否有权限访问该资源）
            boolean hasPermission = employee.getGroups().stream()
                    .flatMap(group -> group.getResources().stream())
                    .anyMatch(r -> r.getResourceId().equals(resource.getResourceId()));

            if (!hasPermission) {
                AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.NO_PERMISSION, "没有访问该资源的权限");
                recordLog(badge, employee, resource, result, request);
                return result;
            }

            // 7. 验证资源状态
            if (resource.getResourceState() == ResourceState.LOCKED) {
                AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.RESOURCE_LOCKED, "资源已被锁定");
                recordLog(badge, employee, resource, result, request);
                return result;
            }
            if (resource.getResourceState() == ResourceState.OCCUPIED) {
                AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.RESOURCE_OCCUPIED, "资源当前被占用");
                recordLog(badge, employee, resource, result, request);
                return result;
            }

            // 8. 所有验证通过，允许访问
            AccessResult result = new AccessResult(AccessDecision.ALLOW, ReasonCode.ALLOW, "允许访问");
            recordLog(badge, employee, resource, result, request);
            return result;

        } catch (Exception e) {
            // 处理系统异常
            AccessResult result = new AccessResult(AccessDecision.DENY, ReasonCode.SYSTEM_ERROR, "系统内部错误");
            recordLog(null, null, null, result, request);
            return result;
        }
    }

    // 记录访问日志
    private void recordLog(Badge badge, Employee employee, Resource resource, AccessResult result, AccessRequest request) {
        LogEntry logEntry = new LogEntry(
                LocalDateTime.ofInstant(request.getTimestamp(), ZoneId.systemDefault()),
                badge,
                employee,
                resource,
                result.getDecision(),
                result.getReasonCode()
        );
        logService.record(logEntry);
    }
}