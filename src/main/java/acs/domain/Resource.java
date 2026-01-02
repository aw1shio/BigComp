package acs.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
/**
 * Resource 表示受控资源（门、房间、设备等）。
 *
 * 设计原则：
 * - ResourceType 描述资源类型（DOOR/PRINTER/...）
 * - ResourceState 描述资源状态（AVAILABLE/OCCUPIED/LOCKED/OFFLINE）
 * - 权限不直接写在 Resource 中（我们选择由 Group 管理授权列表）
 */
@Entity
public class Resource {

    /** 资源唯一 ID（例如：R-DOOR-301） */
    @Id
    private String id;

    /** 资源名称（例如：Door 301 / Printer 2F） */
    private String name;

    /** 资源类型 */
    private ResourceType type;

    /** 资源状态 */
    private ResourceState state;

    // 无参构造器（JPA实体类必须提供，否则无法通过反射实例化）
    public Resource() {
    }

    public Resource(String id, String name, ResourceType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.state = ResourceState.AVAILABLE; // 默认可用
    }

    public Resource(String id, String name, ResourceType type, ResourceState state) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public ResourceState getState() {
        return state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public void setState(ResourceState state) {
        this.state = state;
    }
}
