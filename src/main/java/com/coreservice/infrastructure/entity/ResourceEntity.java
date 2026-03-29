package com.coreservice.infrastructure.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resources")
public class ResourceEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    protected ResourceEntity() {
        // JPA only
    }

    public ResourceEntity(String id, String name, String description) {
        this.id = id;
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
}
