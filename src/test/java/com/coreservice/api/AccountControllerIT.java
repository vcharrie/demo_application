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

        // Préparation : insérer des comptes en base

        var acc1 = new AccountEntity(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(100), AccountStatus.ACTIVE);
        var acc2 = new AccountEntity(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(200), AccountStatus.ACTIVE);

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
        var entity = new AccountEntity(id, ownerId, BigDecimal.valueOf(150), status);

        repository.save(entity);

        mockMvc.perform(
                get("/api/accounts/" + id)
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
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
        var entity = new AccountEntity(id, ownerId, BigDecimal.valueOf(150), status);

        repository.save(entity);

        mockMvc.perform(
                delete("/api/accounts/" + id)
                    .header("Authorization", "Basic " + basic)
            )
            .andExpect(status().isNoContent());

        // Vérification : le compte n'existe plus
        assertFalse(repository.findById(id).isPresent());
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

}