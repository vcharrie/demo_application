package com.coreservice.application;

import com.coreservice.domain.Resource;
import com.coreservice.domain.exception.ResourceConflictException;
import com.coreservice.domain.exception.ResourceNotFoundException;
import com.coreservice.infrastructure.entity.ResourceEntity;
import com.coreservice.infrastructure.repository.ResourceRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceServiceImplTest {

    private final ResourceRepository repository = mock(ResourceRepository.class);
    private final ResourceServiceImpl service = new ResourceServiceImpl(repository);

    @Test
    void shouldCreateResource() {
        Resource resource = new Resource("id-1", "Test", "desc");

        when(repository.findByName("Test")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(new ResourceEntity("id-1", "Test", "desc"));

        Resource result = service.create(resource);

        assertThat(result.getId()).isEqualTo("id-1");
        verify(repository).save(any());
    }

    @Test
    void shouldThrowConflictWhenNameExists() {
        when(repository.findByName("Test")).thenReturn(Optional.of(new ResourceEntity("id-1", "Test", "desc")));

        assertThatThrownBy(() -> service.create(new Resource("id-2", "Test", "desc")))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void shouldFindAll() {
        when(repository.findAll()).thenReturn(List.of(new ResourceEntity("id-1", "Test", "desc")));

        List<Resource> result = service.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldThrowNotFoundWhenMissing() {
        when(repository.findById("id-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("id-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

