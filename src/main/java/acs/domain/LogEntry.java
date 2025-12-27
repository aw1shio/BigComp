package acs.domain;

import java.time.Instant;

/**
 * LogEntry 表示一条“访问日志记录”
 *
 * ⚠️ 设计要求：
 * - 每一次访问（无论成功还是失败）都必须生成一条 LogEntry
 * - 日志是企业级访问控制系统的核心组成部分
 */
public class LogEntry {

    /** 访问发生的时间 */
    private Instant timestamp;

    /** 使用的 Badge ID */
    private String badgeId;

    /** 对应的员工 ID（如果能解析到） */
    private String employeeId;

    /** 被访问的资源 ID */
    private String resourceId;

    /** 最终访问决策 */
    private AccessDecision decision;

    /** 访问结果原因码 */
    private ReasonCode reasonCode;

    public LogEntry(Instant timestamp,
                    String badgeId,
                    String employeeId,
                    String resourceId,
                    AccessDecision decision,
                    ReasonCode reasonCode) {
        this.timestamp = timestamp;
        this.badgeId = badgeId;
        this.employeeId = employeeId;
        this.resourceId = resourceId;
        this.decision = decision;
        this.reasonCode = reasonCode;
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

    public AccessDecision getDecision() {
        return decision;
    }

    public ReasonCode getReasonCode() {
        return reasonCode;
    }
}
