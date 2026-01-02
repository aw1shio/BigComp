package acs.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table; 

/**
 * Group 表示一类权限角色（例如 Admin / Staff / Visitor）。
 *
 * 我们采用一种简单、易解释的权限模型：
 * - Group 持有自己能访问的 ResourceId 集合（resourceIds）
 * - 判断权限时：只要员工所属任意 group 的 resourceIds 包含该资源，即可访问
 *
 * 优点：
 * - 逻辑清晰，答辩好讲
 * - 数据驱动，配置权限只需改 Group 的授权列表
 */
@Entity
@Table(name = "permission_group") 
public class Group {

    /** 组唯一 ID（例如：G-DEV） */
    @Id
    private String id;

    /** 组名（用于 UI 展示） */
    private String name;

    /**
     * 本组被授权访问的资源 ID 集合
     * - 例如：G-DEV 可以访问 R-DOOR-301, R-PRINTER-2F
     */
    private final Set<String> resourceIds = new HashSet<>();

    // 无参构造器（JPA实体类必须提供，否则Spring Data JPA无法通过反射实例化实体）
    public Group() {
    }

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Group(String id, String name, Set<String> resourceIds) {
        this.id = id;
        this.name = name;
        if (resourceIds != null) {
            this.resourceIds.addAll(resourceIds);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回授权资源集合的只读视图，避免外部直接改内部集合。
     */
    public Set<String> getResourceIds() {
        return Collections.unmodifiableSet(resourceIds);
    }

    /** 授权本组访问某资源（AdminService 可调用） */
    public void grantResource(String resourceId) {
        if (resourceId != null && !resourceId.isBlank()) {
            resourceIds.add(resourceId);
        }
    }

    /** 撤销本组对某资源的访问权限（AdminService 可调用） */
    public void revokeResource(String resourceId) {
        if (resourceId != null && !resourceId.isBlank()) {
            resourceIds.remove(resourceId);
        }
    }
}
