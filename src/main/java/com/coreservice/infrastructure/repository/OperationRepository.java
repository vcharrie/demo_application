package com.coreservice.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coreservice.infrastructure.entity.OperationEntity;

public interface OperationRepository extends JpaRepository<OperationEntity, UUID> {
}