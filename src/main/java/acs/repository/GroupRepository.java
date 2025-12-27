package acs.repository;

import acs.domain.Group;

import java.util.Optional;

public interface GroupRepository {

    Optional<Group> findById(String groupId);

    void save(Group group);
}
