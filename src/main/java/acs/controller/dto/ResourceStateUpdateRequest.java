package acs.controller.dto;

import acs.domain.ResourceState;

/**
 * 更新资源状态的请求体
 */
public class ResourceStateUpdateRequest {
    private ResourceState state;

    public ResourceState getState() {
        return state;
    }

    public void setState(ResourceState state) {
        this.state = state;
    }
}
