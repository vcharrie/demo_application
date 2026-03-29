package com.coreservice.infrastructure.repository;

import com.coreservice.infrastructure.entity.ResourceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ResourceRepositoryIT {

    @Autowired
    private ResourceRepository repository;

    @Test
    void shouldSaveAndRetrieveResource() {
        ResourceEntity entity = new ResourceEntity("id-1", "Test", "desc");
        repository.save(entity);

        Optional<ResourceEntity> found = repository.findById("id-1");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test");
    }

    @Test
    void shouldFindByName() {
        repository.save(new ResourceEntity("id-2", "UniqueName", "desc"));

        Optional<ResourceEntity> found = repository.findByName("UniqueName");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("id-2");
    }
}