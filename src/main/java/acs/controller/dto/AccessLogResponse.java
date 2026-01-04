package acs.controller.dto;

import acs.domain.AccessDecision;
import acs.domain.LogEntry;
import acs.domain.ReasonCode;

import java.time.Instant;
import java.time.ZoneId;

/**
 * 对外暴露的访问日志响应体，避免直接序列化实体导致循环引用
 */
public class AccessLogResponse {
    private Long id;
    private Instant timestamp;
    private String badgeId;
    private String employeeId;
    private String resourceId;
    private String resourceName;
    private AccessDecision decision;
    private ReasonCode reasonCode;

    public static AccessLogResponse from(LogEntry entry) {
        AccessLogResponse response = new AccessLogResponse();
        response.id = entry.getId();
        response.timestamp = entry.getTimestamp().atZone(ZoneId.systemDefault()).toInstant();
        response.badgeId = entry.getBadge() != null ? entry.getBadge().getBadgeId() : null;
        response.employeeId = entry.getEmployee() != null ? entry.getEmployee().getEmployeeId() : null;
        response.resourceId = entry.getResource() != null ? entry.getResource().getResourceId() : null;
        response.resourceName = entry.getResource() != null ? entry.getResource().getResourceName() : null;
        response.decision = entry.getDecision();
        response.reasonCode = entry.getReasonCode();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public AccessDecision getDecision() {
        return decision;
    }

    public ReasonCode getReasonCode() {
        return reasonCode;
    }
}
