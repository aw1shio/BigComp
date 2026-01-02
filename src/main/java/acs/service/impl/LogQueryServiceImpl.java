package acs.service.impl;

import acs.domain.LogEntry;
import acs.repository.AccessLogRepository;
import acs.service.LogQueryService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogQueryServiceImpl implements LogQueryService {

    private final AccessLogRepository logRepository;

    public LogQueryServiceImpl(AccessLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public List<LogEntry> findByBadge(String badgeId, Instant from, Instant to) {
        LocalDateTime start = LocalDateTime.ofInstant(from, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(to, ZoneId.systemDefault());
        
        return logRepository.findAll().stream()
                .filter(log -> log.getBadge() != null 
                        && log.getBadge().getBadgeId().equals(badgeId)
                        && !log.getTimestamp().isBefore(start)
                        && !log.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<LogEntry> findByEmployee(String employeeId, Instant from, Instant to) {
        LocalDateTime start = LocalDateTime.ofInstant(from, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(to, ZoneId.systemDefault());
        
        return logRepository.findAll().stream()
                .filter(log -> log.getEmployee() != null 
                        && log.getEmployee().getEmployeeId().equals(employeeId)
                        && !log.getTimestamp().isBefore(start)
                        && !log.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<LogEntry> findByResource(String resourceId, Instant from, Instant to) {
        LocalDateTime start = LocalDateTime.ofInstant(from, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(to, ZoneId.systemDefault());
        
        return logRepository.findAll().stream()
                .filter(log -> log.getResource() != null 
                        && log.getResource().getResourceId().equals(resourceId)
                        && !log.getTimestamp().isBefore(start)
                        && !log.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<LogEntry> findDenied(Instant from, Instant to) {
        LocalDateTime start = LocalDateTime.ofInstant(from, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(to, ZoneId.systemDefault());
        
        return logRepository.findAll().stream()
                .filter(log -> log.getDecision().name().equals("DENY")
                        && !log.getTimestamp().isBefore(start)
                        && !log.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }
}