package acs.domain;

/**
 * Badge 表示员工使用的“徽章/卡片/凭证”。
 *
 * 设计原则：
 * - Badge 是逻辑凭证（logical credential），不是物理芯片建模
 * - status 用于启用/禁用/挂失
 * - employeeId 用于绑定员工（可为空表示未分配）
 */
public class Badge {

    /** 徽章唯一 ID（例如：B-10001） */
    private String id;

    /** 徽章状态：ACTIVE / DISABLED / LOST */
    private BadgeStatus status;

    /**
     * 当前徽章绑定的员工 ID
     * - 可为空：表示该徽章未分配给任何员工
     */
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
