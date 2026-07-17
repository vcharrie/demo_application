package com.coreservice.application;

import com.coreservice.domain.Resource;
import com.coreservice.domain.exception.ResourceConflictException;
import com.coreservice.domain.exception.ResourceNotFoundException;
import com.coreservice.infrastructure.entity.ResourceEntity;
import com.coreservice.infrastructure.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResourceServiceImplTest {

    private ResourceRepository repository;
    private ResourceServiceImpl service;

    @BeforeEach
    void setup() {
        repository = mock(ResourceRepository.class);
        service = new ResourceServiceImpl(repository);
    }

    // ------------------------------------------------------------
    // TESTS MÉTIER
    // ------------------------------------------------------------

    @Test
    void shouldCreateResource() {
        UUID randomUuid = UUID.randomUUID();
        Resource resource = new Resource(randomUuid.toString(), "Test", "desc");

        when(repository.findByName("Test")).thenReturn(Optional.empty());
        when(repository.save(any(ResourceEntity.class)))
                .thenReturn(new ResourceEntity(randomUuid, "Test", "desc"));

        Resource result = service.create(resource);

        assertThat(result.getId()).isEqualTo(randomUuid.toString());
        verify(repository).save(any(ResourceEntity.class));
    }

    @Test
    void shouldThrowConflictWhenNameExists() {
        when(repository.findByName("Test"))
                .thenReturn(Optional.of(new ResourceEntity(UUID.randomUUID(), "Test", "desc")));

        assertThatThrownBy(() -> service.create(new Resource("id-2", "Test", "desc")))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void shouldFindAll() {
        when(repository.findAll())
                .thenReturn(List.of(new ResourceEntity(UUID.randomUUID(), "Test", "desc")));

        List<Resource> result = service.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldThrowNotFoundWhenMissing() {
        UUID randomUuid = UUID.randomUUID();
        when(repository.findById(randomUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(randomUuid.toString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
