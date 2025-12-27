package acs.repository;

import acs.domain.Resource;

import java.util.Optional;

public interface ResourceRepository {

    Optional<Resource> findById(String resourceId);

    void save(Resource resource);
}
