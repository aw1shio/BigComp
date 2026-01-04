package acs.controller.dto;

import acs.domain.BadgeStatus;

/**
 * 更新徽章状态的请求体
 */
public class BadgeStatusUpdateRequest {
    private BadgeStatus status;

    public BadgeStatus getStatus() {
        return status;
    }

    public void setStatus(BadgeStatus status) {
        this.status = status;
    }
}
