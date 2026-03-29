package com.coreservice.infrastructure.mapper;

import com.coreservice.domain.Resource;
import com.coreservice.infrastructure.entity.ResourceEntity;

public final class ResourceMapper {

    private ResourceMapper() {}

    public static ResourceEntity toEntity(Resource resource) {
        return new ResourceEntity(
                resource.getId(),
                resource.getName(),
                resource.getDescription()
        );
    }

    public static Resource toDomain(ResourceEntity entity) {
        return new Resource(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}

