package com.coreservice.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coreservice.infrastructure.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    boolean existsByOwnerId(UUID ownerId);
}