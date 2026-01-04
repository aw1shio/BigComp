package acs.controller;

import acs.controller.dto.AccessCheckRequest;
import acs.domain.AccessRequest;
import acs.domain.AccessResult;
import acs.service.AccessControlService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    private final AccessControlService accessControlService;

    public AccessController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    /**
     * 前端调用访问判定入口
     */
    @PostMapping("/decide")
    @ResponseStatus(HttpStatus.OK)
    public AccessResult decide(@RequestBody AccessCheckRequest request) {
        Instant timestamp = request.getTimestamp() != null ? request.getTimestamp() : Instant.now();
        AccessRequest accessRequest = new AccessRequest(
                request.getBadgeId(),
                request.getResourceId(),
                timestamp
        );
        return accessControlService.processAccess(accessRequest);
    }
}
