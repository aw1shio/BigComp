package acs.repository;

import acs.domain.LogEntry;

import java.util.List;

public interface AccessLogRepository {

    void append(LogEntry entry);

    List<LogEntry> findAll();
}
