package acs.repository;

import acs.domain.LogEntry;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessLogRepository extends JpaRepository<LogEntry, Long> {

    List<LogEntry> findAll();
}
