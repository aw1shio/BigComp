package acs.repository;

import acs.domain.Badge;

import java.util.Optional;

public interface BadgeRepository {

    Optional<Badge> findById(String badgeId);

    void save(Badge badge);
}
