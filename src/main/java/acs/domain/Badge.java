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
@Table(name = "badges") // 对应数据库中的badges表（可自定义表名）
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 徽章唯一 ID（例如：B-10001） */
    private String id;

    @Column(name = "badge_id", unique = true, nullable = false)
    /** 徽章状态：ACTIVE / DISABLED / LOST */
    private BadgeStatus status;

    /**
     * 当前徽章绑定的员工 ID
     * - 可为空：表示该徽章未分配给任何员工
     */
    @Column(name = "employee_id")
    private String employeeId;

    public Badge(String id) {
        this.id = id;
        this.status = BadgeStatus.ACTIVE;
    }

    public Badge(String id, BadgeStatus status, String employeeId) {
        this.id = id;
        this.status = status;
        this.employeeId = employeeId;
    }

    public String getId() {
        return id;
    }

    public BadgeStatus getStatus() {
        return status;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setStatus(BadgeStatus status) {
        this.status = status;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
