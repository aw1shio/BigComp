package acs.domain;

import java.time.Instant;

import jakarta.persistence.*;

/**
 * LogEntry 表示一条“访问日志记录”
 *
 * 
 * - 每一次访问（无论成功还是失败）都必须生成一条 LogEntry
 * - 日志是企业级访问控制系统的核心组成部分
 */
@Entity
@Table(name = "access_logs") // 对应数据库表名
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    private Long id; // 主键字段

    /** 访问发生的时间 */
    @Column(name = "timestamp")
    private Instant timestamp;

    /** 使用的 Badge ID */
    @Column(name = "badge_id")
    private String badgeId;

    /** 对应的员工 ID（如果能解析到） */
    @Column(name = "employee_id")
    private String employeeId;

    /** 被访问的资源 ID */
    @Column(name = "resource_id")
    private String resourceId;

    /** 最终访问决策 */
    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private AccessDecision decision;

    /** 访问结果原因码 */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code")
    private ReasonCode reasonCode;

    public LogEntry() {
    }

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
