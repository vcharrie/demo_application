package com.coreservice.domain;

import java.util.Objects;

public class Resource {

    private final String id;
    private final String name;
    private final String description;

    
    public Resource(String name, String description) {
        this.id = null;
        this.name = name;
        this.description = description;
    }


    public Resource(String id, String name, String description) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.description = description;
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

