package acs.repository.impl;

import acs.domain.Badge;
import acs.repository.BadgeRepository;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * 线程安全的内存版 BadgeRepository。
 *
 * 用途：
 * - DB 不可用/未接入时也能支撑访问验证
 * - 支撑并发请求下的安全读写
 */
@Repository
public class InMemoryBadgeRepository implements BadgeRepository {

    private final ConcurrentHashMap<String, Badge> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Badge> findById(String badgeId) {
        if (badgeId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(badgeId));
    }

    @Override
    public void save(Badge badge) {
        if (badge == null || badge.getId() == null) {
            return;
        }
        store.put(badge.getId(), badge);
    }

    private final Map<String, Instant> expirations = new ConcurrentHashMap<>();

    public void setExpiration(String badgeId, Instant expiration) {
        expirations.put(badgeId, expiration);
    }

    public Optional<Instant> getExpiration(String badgeId) {
        return Optional.ofNullable(expirations.get(badgeId));
    }
}
