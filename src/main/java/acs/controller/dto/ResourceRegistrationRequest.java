package acs.controller.dto;

import acs.domain.ResourceType;

/**
 * 注册新资源的请求体
 */
public class ResourceRegistrationRequest {
    private String resourceId;
    private String name;
    private ResourceType type;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }
}
