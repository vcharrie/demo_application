package com.coreservice.domain;

import java.util.Objects;

import jakarta.annotation.Generated;

public class Resource {

    private final String id;
    private final String name;
    private final String description;

    public Resource(String id, String name, String description) {
        validateName(name);
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = name;
        this.description = description;
    }

    public Resource(String name, String description) {
        validateName(name);
        this.id = null;
        this.name = name;
        this.description = description;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Resource name must not be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Resource name must not exceed 100 characters");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // equals/hashCode pour permettre les tests et comparaisons
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        Resource resource = (Resource) o;
        return id.equals(resource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

