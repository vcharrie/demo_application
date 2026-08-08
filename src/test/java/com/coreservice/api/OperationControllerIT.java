package com.coreservice.api;

import com.coreservice.infrastructure.entity.AccountEntity;
import com.coreservice.infrastructure.entity.OperationEntity;
import com.coreservice.infrastructure.entity.OperationStatus;
import com.coreservice.infrastructure.entity.OperationType;
import com.coreservice.infrastructure.repository.AccountRepository;
import com.coreservice.infrastructure.repository.OperationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestUsersConfig.class)
class OperationControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    OperationRepository operationRepository;

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
    }

    @Test
    void shouldReturn200AndOperationsList() throws Exception {
        UUID accountOwnerId = UUID.randomUUID();

        // Préparation : créer un compte
        AccountEntity account = new AccountEntity(accountOwnerId, BigDecimal.ZERO, com.coreservice.domain.AccountStatus.ACTIVE);
        AccountEntity accountSaved = accountRepository.save(account);

        // Préparation : créer deux opérations
        OperationEntity op1 = new OperationEntity(
                OperationType.TRANSFER,
                OperationStatus.COMPLETED,
                accountSaved.getId(),
                UUID.randomUUID(),
                BigDecimal.TEN
        );

        OperationEntity op2 = new OperationEntity(
                OperationType.TRANSFER,
                OperationStatus.PENDING,
                UUID.randomUUID(),
                accountSaved.getId(),
                BigDecimal.ONE
        );

        operationRepository.save(op1);
        operationRepository.save(op2);

        mockMvc.perform(get("/api/accounts/{id}/operations", accountSaved.getId())
                        .header("Authorization", basicAuth())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountSaved.getId().toString()))
                .andExpect(jsonPath("$.operations").isArray())
                .andExpect(jsonPath("$.operations.length()").value(2))
                .andExpect(jsonPath("$.operations[0].sourceAccountId").value(accountSaved.getId().toString()))
                .andExpect(jsonPath("$.operations[1].destinationAccountId").value(accountSaved.getId().toString()));
    }

    @Test
    void shouldReturn200WithEmptyList() throws Exception {
        UUID ownerId = UUID.randomUUID();

        // Préparation : créer un compte sans opérations
        AccountEntity account = new AccountEntity(ownerId, BigDecimal.ZERO, com.coreservice.domain.AccountStatus.ACTIVE);
        AccountEntity saved = accountRepository.save(account);

        mockMvc.perform(get("/api/accounts/{id}/operations", saved.getId())
                        .header("Authorization", basicAuth())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(saved.getId().toString()))
                .andExpect(jsonPath("$.operations").isArray())
                .andExpect(jsonPath("$.operations.length()").value(0));
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/accounts/{id}/operations", unknownId)
                        .header("Authorization", basicAuth())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ACCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/accounts/not-a-uuid/operations")
                        .header("Authorization", basicAuth())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/accounts/{id}/operations", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
    }

    @Test
    void shouldReturn401WhenWrongCredentials() throws Exception {
        UUID id = UUID.randomUUID();

        String wrongBasic = "Basic " + Base64.getEncoder().encodeToString("other:password".getBytes());

        mockMvc.perform(get("/api/accounts/{id}/operations", id)
                        .header("Authorization", wrongBasic)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
    }

}

