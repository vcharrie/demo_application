package com.coreservice.domain;

import java.time.Instant;
import java.util.UUID;

public class AuditEvent {

    private UUID id; // pas final → OK

    private final Instant timestamp;
    private final String type;
    private final String details;

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public AuditEvent(String type, String details) {
        this.type = type;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; } // appelé après persistance
}