package com.coreservice.application;

import com.coreservice.domain.Resource;
import com.coreservice.domain.ResourceService;
import com.coreservice.domain.exception.ResourceConflictException;
import com.coreservice.domain.exception.ResourceNotFoundException;
import com.coreservice.infrastructure.mapper.ResourceMapper;
import com.coreservice.infrastructure.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository repository;

    public ResourceServiceImpl(ResourceRepository repository) {
        this.repository = repository;
    }

    private UUID validateAndParseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }
    }

    @Override
    public Resource create(Resource resource) {
        repository.findByName(resource.getName())
                .ifPresent(r -> { throw new ResourceConflictException(resource.getName()); });

        var entity = ResourceMapper.toEntity(resource);
        var saved = repository.save(entity);

        return ResourceMapper.toDomain(saved);
    }

    @Override
    public List<Resource> findAll() {
        return repository.findAll()
                .stream()
                .map(ResourceMapper::toDomain)
                .toList();
    }

    @Override
    public Resource findById(String id) {
        UUID uuid = validateAndParseUuid(id);
        return repository.findById(uuid)
                .map(ResourceMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        repository.deleteById(id);
    }
}

