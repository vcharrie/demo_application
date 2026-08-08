package com.coreservice.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coreservice.infrastructure.entity.OperationEntity;

public interface OperationRepository extends JpaRepository<OperationEntity, UUID> {


    List<OperationEntity> findBySourceAccountIdOrDestinationAccountId(UUID sourceAccountId,
                                                                      UUID destinationAccountId); 
}