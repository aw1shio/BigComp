package acs.controller.dto;

/**
 * 发放徽章的请求体
 */
public class IssueBadgeRequest {
    private String employeeId;
    private String badgeId;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }
}
