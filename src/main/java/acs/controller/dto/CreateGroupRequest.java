package acs.controller.dto;

/**
 * 创建权限组的请求体
 */
public class CreateGroupRequest {
    private String groupId;
    private String name;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
