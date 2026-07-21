package com.coreservice.api;

import com.coreservice.api.dto.AccountRequest;
import com.coreservice.domain.AccountStatus;
import com.coreservice.infrastructure.entity.AccountEntity;
import com.coreservice.infrastructure.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Import(TestUsersConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AccountControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    AccountRepository repository;

    @Test
    void shouldCreateAccount() throws Exception {
        UUID ownerId = UUID.randomUUID();
        var request = new AccountRequest(ownerId, BigDecimal.valueOf(100));

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(
                post("/api/accounts")
                    .header("Authorization", "Basic " + basic)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .content(Objects.requireNonNull(mapper.writeValueAsString(request)))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
            .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidBody() throws Exception {
        // initialBalance négatif → violation @PositiveOrZero
        var request = new AccountRequest(UUID.randomUUID(), BigDecimal.valueOf(-10));

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(
                post("/api/accounts")
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOwnerIdIsInvalidUUID() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        String json = """
            {
                "ownerId": "not-a-uuid",
                "initialBalance": 100
            }
            """;

        mockMvc.perform(
                post("/api/accounts")
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        String basic = Base64.getEncoder().encodeToString("other:password".getBytes());

        UUID ownerId = UUID.randomUUID();
        var request = new AccountRequest(ownerId, BigDecimal.valueOf(100));

        mockMvc.perform(
                post("/api/accounts")
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(status().reason("Unauthorized"));
    }

    @Test
    void shouldReturnAllAccounts() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        // Préparation : supprimer tous les comptes existants pour un test propre
        repository.deleteAll();

        // Préparation : insérer des comptes en base

        var acc1 = new AccountEntity(UUID.randomUUID(), BigDecimal.valueOf(100), AccountStatus.ACTIVE);
        var acc2 = new AccountEntity(UUID.randomUUID(), BigDecimal.valueOf(200), AccountStatus.ACTIVE);

        repository.saveAll(List.of(acc1, acc2));

        mockMvc.perform(
                get("/api/accounts")
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(acc1.getId().toString()))
            .andExpect(jsonPath("$[1].id").value(acc2.getId().toString()));
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        repository.deleteAll(); // On s’assure que la base est vide

        mockMvc.perform(
                get("/api/accounts")
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        // Préparation : insérer un compte en base
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        AccountStatus status = AccountStatus.ACTIVE;
        var entity = new AccountEntity(ownerId, BigDecimal.valueOf(150), status);

        var savedEntity = repository.save(entity);

        mockMvc.perform(
                get("/api/accounts/" + savedEntity.getId())
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedEntity.getId().toString()))
            .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
            .andExpect(jsonPath("$.balance").value(150));
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(
                get("/api/accounts/" + unknownId)
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isNotFound());
    }
    
    @Test
    void shouldDeleteAccountSuccessfully() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        // Préparation : insérer un compte en base
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        AccountStatus status = AccountStatus.ACTIVE;
        var entity = new AccountEntity(ownerId, BigDecimal.valueOf(150), status);

        repository.save(entity);

        mockMvc.perform(
                delete("/api/accounts/" + entity.getId())
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isNoContent());

        // Vérification : le compte n'existe plus
        assertFalse(repository.findById(entity.getId()).isPresent());
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExistInDelete() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(
                delete("/api/accounts/" + unknownId)
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenIdIsInvalidUUID() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(
                delete("/api/accounts/not-a-uuid")
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void depositShouldReturn200AndUpdateBalance() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());
        
        // Arrange : créer un compte en base
        UUID ownerId = UUID.randomUUID();
        AccountEntity entity = new AccountEntity(ownerId, BigDecimal.ZERO, AccountStatus.ACTIVE);
        AccountEntity entitySaved = repository.save(entity);

        // Act + Assert
        mockMvc.perform(post("/api/accounts/" + entitySaved.getId() + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(10));

        // Vérification en base
        AccountEntity updated = repository.findById(entitySaved.getId()).orElseThrow();
        assertEquals(new BigDecimal("10.00"), updated.getBalance());
    }

    @Test
    void depositShouldReturn400WhenAmountIsInvalid() throws Exception {
        UUID id = UUID.randomUUID();

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(post("/api/accounts/" + id + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 0}
                                """))
                .andExpect(status().isBadRequest());
    }


    @Test
    void depositShouldReturn404WhenAccountNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(post("/api/accounts/" + id + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 10}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void depositShouldReturn409WhenAccountSuspended() throws Exception {
        // Arrange : compte suspendu
        UUID id = UUID.randomUUID();
        AccountEntity entity = new AccountEntity(
            UUID.randomUUID(),
            BigDecimal.ZERO,
            AccountStatus.SUSPENDED
        );

        AccountEntity entitySaved = repository.save(entity);

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(post("/api/accounts/" + entitySaved.getId() + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 10}
                                """))
                .andExpect(status().isConflict()); // ou 422 selon ton mapping
    }

    @Test
    void withdrawShouldReturn200AndUpdateBalance() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        // Arrange : créer un compte en base
        UUID ownerId = UUID.randomUUID();
        AccountEntity entity = new AccountEntity(ownerId, BigDecimal.valueOf(100), AccountStatus.ACTIVE);
        AccountEntity saved = repository.save(entity);

        // Act + Assert
        mockMvc.perform(post("/api/accounts/" + saved.getId() + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70));

        // Vérification en base
        AccountEntity updated = repository.findById(saved.getId()).orElseThrow();
        assertEquals(new BigDecimal("70.00"), updated.getBalance());
    }

    @Test
    void withdrawShouldReturn400WhenAmountIsInvalid() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/accounts/" + id + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdrawShouldReturn404WhenAccountNotFound() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(post("/api/accounts/" + unknownId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 10}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void withdrawShouldReturn409WhenAccountSuspended() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        // Arrange : compte suspendu
        UUID ownerId = UUID.randomUUID();
        AccountEntity entity = new AccountEntity(ownerId, BigDecimal.valueOf(100), AccountStatus.SUSPENDED);
        AccountEntity saved = repository.save(entity);

        mockMvc.perform(post("/api/accounts/" + saved.getId() + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 10}
                                """))
                .andExpect(status().isConflict()); // même logique que deposit
    }

    @Test
    void withdrawShouldReturn409WhenBalanceInsufficient() throws Exception {
        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        // Arrange : solde insuffisant
        UUID ownerId = UUID.randomUUID();
        AccountEntity entity = new AccountEntity(ownerId, BigDecimal.valueOf(20), AccountStatus.ACTIVE);
        AccountEntity saved = repository.save(entity);

        mockMvc.perform(post("/api/accounts/" + saved.getId() + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + basic)
                        .content("""
                                {"amount": 50}
                                """))
                .andExpect(status().isConflict()); // ou 422 selon ton mapping
    }


}