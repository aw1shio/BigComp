package acs.repository;

import java.util.List;

import acs.domain.LogEntry;

public interface AccessLogRepository {

    void append(LogEntry entry);

    List<LogEntry> findAll();
}
