package com.coreservice.infrastructure.repository;

import com.coreservice.infrastructure.entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<ResourceEntity, String> {

    Optional<ResourceEntity> findByName(String name);

}
