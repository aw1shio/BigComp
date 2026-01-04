package acs.controller;

import acs.controller.dto.AssignEmployeeRequest;
import acs.controller.dto.BadgeStatusUpdateRequest;
import acs.controller.dto.CreateGroupRequest;
import acs.controller.dto.IssueBadgeRequest;
import acs.controller.dto.RegisterEmployeeRequest;
import acs.controller.dto.ResourceRegistrationRequest;
import acs.controller.dto.ResourceStateUpdateRequest;
import acs.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerEmployee(@RequestBody RegisterEmployeeRequest request) {
        adminService.registerEmployee(request.getEmployeeId(), request.getName());
    }

    @PostMapping("/badges")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void issueBadge(@RequestBody IssueBadgeRequest request) {
        adminService.issueBadge(request.getEmployeeId(), request.getBadgeId());
    }

    @PatchMapping("/badges/{badgeId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBadgeStatus(@PathVariable String badgeId,
                                  @RequestBody BadgeStatusUpdateRequest request) {
        adminService.setBadgeStatus(badgeId, request.getStatus());
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createGroup(@RequestBody CreateGroupRequest request) {
        adminService.createGroup(request.getGroupId(), request.getName());
    }

    @PostMapping("/groups/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignEmployeeToGroup(@PathVariable String groupId,
                                      @RequestBody AssignEmployeeRequest request) {
        adminService.assignEmployeeToGroup(request.getEmployeeId(), groupId);
    }

    @DeleteMapping("/groups/{groupId}/members/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployeeFromGroup(@PathVariable String groupId,
                                        @PathVariable String employeeId) {
        adminService.removeEmployeeFromGroup(employeeId, groupId);
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerResource(@RequestBody ResourceRegistrationRequest request) {
        adminService.registerResource(request.getResourceId(), request.getName(), request.getType());
    }

    @PatchMapping("/resources/{resourceId}/state")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateResourceState(@PathVariable String resourceId,
                                    @RequestBody ResourceStateUpdateRequest request) {
        adminService.setResourceState(resourceId, request.getState());
    }

    @PostMapping("/groups/{groupId}/resources/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grantGroupAccess(@PathVariable String groupId,
                                 @PathVariable String resourceId) {
        adminService.grantGroupAccessToResource(groupId, resourceId);
    }

    @DeleteMapping("/groups/{groupId}/resources/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeGroupAccess(@PathVariable String groupId,
                                  @PathVariable String resourceId) {
        adminService.revokeGroupAccessToResource(groupId, resourceId);
    }
}
