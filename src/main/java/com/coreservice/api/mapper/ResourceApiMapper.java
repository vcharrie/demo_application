package com.coreservice.api.mapper;

import com.coreservice.api.dto.ResourceRequest;
import com.coreservice.api.dto.ResourceResponse;
import com.coreservice.domain.Resource;

public class ResourceApiMapper {

    public static Resource toDomain(ResourceRequest request) {
        return new Resource(request.name(), request.description());
    }

    public static ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription()
        );
    }
}
