package acs.log.impl;

import acs.domain.LogEntry;
import acs.log.LogService;
import acs.repository.AccessLogRepository;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private final AccessLogRepository logRepo;

    public LogServiceImpl(AccessLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    @Override
    public void record(LogEntry entry) {
        logRepo.append(entry);
    }
}