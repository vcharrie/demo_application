package com.coreservice.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.coreservice.api.dto.AccountHistoryResponse;
import com.coreservice.api.dto.Operation;
import com.coreservice.application.exception.FunctionalError;
import com.coreservice.application.exception.FunctionalException;
import com.coreservice.application.OperationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestUsersConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationService operationService;

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
    }

    @Test
    void shouldReturn200AndOperationsList() throws Exception {
        UUID accountId = UUID.randomUUID();

        Operation op1 = new Operation(
                UUID.randomUUID(),
                com.coreservice.api.dto.OperationType.TRANSFER,
                com.coreservice.api.dto.OperationStatus.COMPLETED,
                accountId,
                UUID.randomUUID(),
                BigDecimal.TEN
        );

        Operation op2 = new Operation(
                UUID.randomUUID(),
                com.coreservice.api.dto.OperationType.TRANSFER,
                com.coreservice.api.dto.OperationStatus.PENDING,
                UUID.randomUUID(),
                accountId,
                BigDecimal.ONE
        );

        AccountHistoryResponse response =
                new AccountHistoryResponse(accountId, List.of(op1, op2));

        when(operationService.getAccountHistory(accountId))
                .thenReturn(response);

        mockMvc.perform(get("/api/accounts/{id}/operations", accountId)
                        .header("Authorization", basicAuth())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.operations").isArray())
                .andExpect(jsonPath("$.operations.length()").value(2))
                .andExpect(jsonPath("$.operations[0].sourceAccountId").value(accountId.toString()))
                .andExpect(jsonPath("$.operations[1].destinationAccountId").value(accountId.toString()));
    }

    @Test
    void shouldReturn200WithEmptyList() throws Exception {
        UUID accountId = UUID.randomUUID();

        AccountHistoryResponse response =
                new AccountHistoryResponse(accountId, List.of());

        when(operationService.getAccountHistory(accountId))
                .thenReturn(response);

        mockMvc.perform(get("/api/accounts/{id}/operations", accountId)
                        .header("Authorization", basicAuth())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.operations").isArray())
                .andExpect(jsonPath("$.operations.length()").value(0));
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        UUID accountId = UUID.randomUUID();

        when(operationService.getAccountHistory(accountId))
                .thenThrow(new FunctionalException(
                        FunctionalError.ACCOUNT_NOT_FOUND,
                        accountId.toString()
                ));

        mockMvc.perform(get("/api/accounts/{id}/operations", accountId)
                        .header("Authorization", basicAuth())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ACCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }
}