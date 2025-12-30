package acs.repository.impl;
import acs.domain.Group;
import acs.repository.GroupRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGroupRepository implements GroupRepository {

    private final ConcurrentHashMap<String, Group> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Group> findById(String groupId) {
        if (groupId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(groupId));
    }

    @Override
    public void save(Group group) {
        if (group == null || group.getId() == null) {
            return;
        }
        store.put(group.getId(), group);
    }
}
