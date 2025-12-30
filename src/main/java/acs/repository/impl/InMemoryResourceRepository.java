package acs.repository.impl;

import acs.domain.Resource;
import acs.repository.ResourceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryResourceRepository implements ResourceRepository {

    private final ConcurrentHashMap<String, Resource> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Resource> findById(String resourceId) {
        if (resourceId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(resourceId));
    }

    @Override
    public void save(Resource resource) {
        if (resource == null || resource.getId() == null) {
            return;
        }
        store.put(resource.getId(), resource);
    }
}
