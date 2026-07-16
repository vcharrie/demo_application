package com.coreservice.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coreservice.infrastructure.entity.AuditEventEntity;

import java.util.UUID;

public interface AuditRepository extends JpaRepository<AuditEventEntity, UUID> {
}
