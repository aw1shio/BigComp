package acs.controller;

import acs.controller.dto.AccessLogResponse;
import acs.service.LogQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogQueryService logQueryService;

    public LogController(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping("/badge/{badgeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<AccessLogResponse> findByBadge(@PathVariable String badgeId,
                                               @RequestParam(required = false) Instant from,
                                               @RequestParam(required = false) Instant to) {
        InstantRange range = resolveRange(from, to);
        return logQueryService.findByBadge(badgeId, range.from, range.to)
                .stream()
                .map(AccessLogResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/employee/{employeeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<AccessLogResponse> findByEmployee(@PathVariable String employeeId,
                                                  @RequestParam(required = false) Instant from,
                                                  @RequestParam(required = false) Instant to) {
        InstantRange range = resolveRange(from, to);
        return logQueryService.findByEmployee(employeeId, range.from, range.to)
                .stream()
                .map(AccessLogResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/resource/{resourceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<AccessLogResponse> findByResource(@PathVariable String resourceId,
                                                  @RequestParam(required = false) Instant from,
                                                  @RequestParam(required = false) Instant to) {
        InstantRange range = resolveRange(from, to);
        return logQueryService.findByResource(resourceId, range.from, range.to)
                .stream()
                .map(AccessLogResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/denied")
    @ResponseStatus(HttpStatus.OK)
    public List<AccessLogResponse> findDenied(@RequestParam(required = false) Instant from,
                                              @RequestParam(required = false) Instant to) {
        InstantRange range = resolveRange(from, to);
        return logQueryService.findDenied(range.from, range.to)
                .stream()
                .map(AccessLogResponse::from)
                .collect(Collectors.toList());
    }

    private InstantRange resolveRange(Instant from, Instant to) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : resolvedTo.minus(7, ChronoUnit.DAYS);
        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "`from` must be earlier than `to`");
        }
        return new InstantRange(resolvedFrom, resolvedTo);
    }

    private record InstantRange(Instant from, Instant to) { }
}
