package acs.controller.dto;

/**
 * 将员工加入组的请求体
 */
public class AssignEmployeeRequest {
    private String employeeId;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
