package com.coreservice.infrastructure.mapper;

import org.springframework.lang.NonNull;

import com.coreservice.domain.AuditEvent;
import com.coreservice.infrastructure.entity.AuditEventEntity;

public class AuditMapper {

    @NonNull
    public static AuditEventEntity toEntity(@NonNull AuditEvent event) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setTimestamp(event.getTimestamp());
        entity.setType(event.getType());
        entity.setDetails(event.getDetails());
        return entity;
    }
}
