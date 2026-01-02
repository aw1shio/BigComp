package acs.domain;

import jakarta.persistence.*;

/**
 * Badge 表示员工使用的“徽章/卡片/凭证”。
 *
 * 设计原则：
 * - Badge 是逻辑凭证（logical credential），不是物理芯片建模
 * - status 用于启用/禁用/挂失
 * - employeeId 用于绑定员工（可为空表示未分配）
 */

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @Column(name = "badge_id", nullable = false, length = 50)
    private String badgeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BadgeStatus status;

    @OneToOne(mappedBy = "badge")
    private Employee employee;

    // 无参构造器（JPA必需）
    public Badge() {}

    // 全参构造器
    public Badge(String badgeId, BadgeStatus status) {
        this.badgeId = badgeId;
        this.status = status;
    }

    // Getter和Setter
    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public BadgeStatus getStatus() {
        return status;
    }

    public void setStatus(BadgeStatus status) {
        this.status = status;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}