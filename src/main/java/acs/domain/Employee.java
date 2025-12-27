package acs.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Employee 表示一个系统用户（员工/访客等）。
 *
 * 设计原则：
 * - 仅保存员工基本信息和权限相关关联关系（badgeId、groupIds）
 * - 不包含任何访问控制判断逻辑（判断逻辑应在 service 层）
 */
public class Employee {

    /** 员工唯一 ID（例如：E-0001） */
    private String id;

    /** 员工姓名（用于 UI 展示、日志辅助） */
    private String name;

    /**
     * 员工当前绑定的 BadgeId
     * - 允许为空：例如新员工未发卡、卡已回收等情况
     */
    private String badgeId;

    /**
     * 员工所属的权限组 ID 集合
     * 访问权限通常由 “员工所属组” 与 “组被授权资源” 决定
     */
    private final Set<String> groupIds = new HashSet<>();

    public Employee(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Employee(String id, String name, String badgeId) {
        this.id = id;
        this.name = name;
        this.badgeId = badgeId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    /**
     * 返回员工所属组的只读视图，避免外部直接改内部集合。
     */
    public Set<String> getGroupIds() {
        return Collections.unmodifiableSet(groupIds);
    }

    /** 将员工加入某权限组（service/AdminService 可调用） */
    public void addToGroup(String groupId) {
        if (groupId != null && !groupId.isBlank()) {
            groupIds.add(groupId);
        }
    }

    /** 将员工从某权限组移除（service/AdminService 可调用） */
    public void removeFromGroup(String groupId) {
        if (groupId != null && !groupId.isBlank()) {
            groupIds.remove(groupId);
        }
    }
}
