package com.coreservice.infrastructure.mapper;


import java.util.UUID;

import com.coreservice.domain.Resource;
import com.coreservice.infrastructure.entity.ResourceEntity;

public final class ResourceMapper {

    private ResourceMapper() {}

    public static ResourceEntity toEntity(Resource resource) {
        return new ResourceEntity(
                UUID.fromString(resource.getId()),
                resource.getName(),
                resource.getDescription()
        );
    }

    public static Resource toDomain(ResourceEntity entity) {
        return new Resource(
                entity.getId().toString(),
                entity.getName(),
                entity.getDescription()
        );
    }
}

