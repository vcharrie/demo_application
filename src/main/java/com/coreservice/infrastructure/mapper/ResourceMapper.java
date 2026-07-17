package com.coreservice.infrastructure.mapper;

import java.util.UUID;

import com.coreservice.domain.Resource;
import com.coreservice.infrastructure.entity.ResourceEntity;

import io.micrometer.common.lang.NonNull;

public final class ResourceMapper {

    private ResourceMapper() {
    }

    @NonNull 
    public static ResourceEntity toEntity(@NonNull Resource resource) {
        UUID id = null;
        if (resource.getId() != null) {
            id = UUID.fromString(resource.getId());
        }
        return new ResourceEntity(
                id,
                resource.getName(),
                resource.getDescription());
    }

    public static Resource toDomain(ResourceEntity entity) {
        return new Resource(
                entity.getId().toString(),
                entity.getName(),
                entity.getDescription());
    }
}
