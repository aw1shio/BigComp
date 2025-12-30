package acs.repository.impl;

import acs.domain.LogEntry;
import acs.repository.AccessLogRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程安全的内存版审计日志仓库。
 *
 * 说明：
 * - append 使用无锁队列，适合“写多读少”的日志场景
 * - findAll 返回快照，避免遍历期间并发修改导致的不可预期结果
 */
@Repository
public class InMemoryAccessLogRepository implements AccessLogRepository {

    private final ConcurrentLinkedQueue<LogEntry> logs = new ConcurrentLinkedQueue<>();

    @Override
    public void append(LogEntry entry) {
        if (entry == null) {
            return;
        }
        logs.add(entry);
    }

    @Override
    public List<LogEntry> findAll() {
        return new ArrayList<>(logs);
    }
}
