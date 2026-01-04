package acs.controller.dto;

import java.time.Instant;

/**
 * 前端发起访问判定时使用的请求体
 */
public class AccessCheckRequest {
    private String badgeId;
    private String resourceId;
    private Instant timestamp;

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
