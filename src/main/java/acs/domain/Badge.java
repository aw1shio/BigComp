package acs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Badge 表示员工使用的“徽章/卡片/凭证”。
 *
 * 设计原则：
 * - Badge 是逻辑凭证（logical credential），不是物理芯片建模
 * - status 用于启用/禁用/挂失
 * - employeeId 用于绑定员工（可为空表示未分配）
 */
@Entity
@Table(name = "badges") // 对应数据库badges表，适配项目JPA/hibernate依赖的表映射规范
public class Badge {

    /**
     * 徽章唯一 ID（例如：B-10001）
     * 作为主键，映射数据库badge_id列，非空且唯一
     */
    @Id
    @Column(name = "badge_id", nullable = false, unique = true)
    private String id;

    /**
     * 徽章状态：ACTIVE / DISABLED / LOST
     * 枚举类型按字符串存储，适配项目数据库依赖（MySQL），保证数据可读性和扩展性
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BadgeStatus status;

    /**
     * 当前徽章绑定的员工 ID
     * - 可为空：表示该徽章未分配给任何员工
     * 映射数据库employee_id列，允许为空
     */
    @Column(name = "employee_id", nullable = true)
    private String employeeId;

    // ------------- 必需构造函数（适配JPA/hibernate依赖的反射实例化要求）-------------
    /**
     * 无参构造函数（JPA规范必需，由hibernate反射调用，不可删除）
     */
    public Badge() {
    }

    // ------------- 业务构造函数（贴合你的业务逻辑，保留原有使用习惯）-------------
    /**
     * 初始化徽章（默认状态为ACTIVE）
     * @param id 徽章唯一ID
     */
    public Badge(String id) {
        this.id = id;
        this.status = BadgeStatus.ACTIVE;
    }

    /**
     * 全参数初始化徽章
     * @param id 徽章唯一ID
     * @param status 徽章状态
     * @param employeeId 绑定的员工ID
     */
    public Badge(String id, BadgeStatus status, String employeeId) {
        this.id = id;
        this.status = status;
        this.employeeId = employeeId;
    }

    // ------------- Getter/Setter（适配Spring Data JPA依赖的属性访问要求）-------------
    public String getId() {
        return id;
    }

    /**
     * 主键ID若无需修改，可省略setter；若有业务场景需要更新，保留该setter
     * 建议：主键尽量不修改，如需调整可通过业务逻辑处理
     */
    public void setId(String id) {
        this.id = id;
    }

    public BadgeStatus getStatus() {
        return status;
    }

    public void setStatus(BadgeStatus status) {
        this.status = status;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}