package com.coreservice.api;

import com.coreservice.api.dto.ResourceRequest;
import com.coreservice.api.dto.ResourceResponse;
import com.coreservice.api.mapper.ResourceApiMapper;
import com.coreservice.application.ResourceService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        var created = service.create(ResourceApiMapper.toDomain(request));
        return ResponseEntity.ok(ResourceApiMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponse>> findAll() {
        var resources = service.findAll()
                .stream()
                .map(ResourceApiMapper::toResponse)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getById(
        @PathVariable
        @Pattern(regexp = "^[a-fA-F0-9-]{36}$")
        String id) {
        var resource = service.findById(id);
        return ResponseEntity.ok(ResourceApiMapper.toResponse(resource));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

