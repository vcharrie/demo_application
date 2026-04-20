package com.coreservice.infrastructure.mapper;


import java.util.UUID;

import com.coreservice.domain.Resource;
import com.coreservice.infrastructure.entity.ResourceEntity;

public final class ResourceMapper {

    private ResourceMapper() {}

    public static ResourceEntity toEntity(Resource resource) {
         UUID id = resource.getId() == null
            ? null
            : UUID.fromString(resource.getId());

        
        return new ResourceEntity(
                id,
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

