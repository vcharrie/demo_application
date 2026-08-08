package com.coreservice.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.coreservice.api.dto.TransferValidationResult;
import com.coreservice.application.TransferService;
import com.coreservice.application.exception.TechnicalError;
import com.coreservice.application.exception.TechnicalException;
import com.coreservice.domain.OperationStatus;
import com.coreservice.domain.Transfer;
import com.coreservice.domain.exception.BusinessError;
import com.coreservice.domain.exception.BusinessException;
import com.coreservice.exception.GlobalExceptionHandler;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Import(TestUsersConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Mock
    private GlobalExceptionHandler globalExceptionHandler;

    

    @Test
    void shouldReturn200AndTransferResultWhenNominalCase() throws Exception {
        // GIVEN
        UUID src = UUID.randomUUID();
        UUID dst = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");

        Transfer transfer = new Transfer(src, dst, amount);
        transfer.setStatus(OperationStatus.COMPLETED);

        when(transferService.initiateTransfer(src, dst, amount))
                .thenReturn(transfer);

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());        

        // WHEN + THEN
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Basic " + basic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "amount": 100
                                }
                                """.formatted(src, dst)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceAccountId").value(src.toString()))
                .andExpect(jsonPath("$.destinationAccountId").value(dst.toString()))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldReturn400WhenBusinessExceptionIsThrown() throws Exception {
        // GIVEN
        UUID src = UUID.randomUUID();
        UUID dst = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");
    
        // GIVEN
        when(transferService.initiateTransfer(src, dst, amount))
                .thenThrow(new BusinessException(BusinessError.TRANSFER_SAME_ACCOUNT, src.toString()));

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());        

        // WHEN + THEN
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Basic " + basic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "amount": 100
                                }
                                """.formatted(src, dst)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TRANSFER_SAME_ACCOUNT"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn500WhenTechnicalExceptionIsThrown() throws Exception {
        // GIVEN
        // GIVEN
        UUID src = UUID.randomUUID();
        UUID dst = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");
        when(transferService.initiateTransfer(src, dst, amount))
                .thenThrow(new TechnicalException(TechnicalError.TRANSFER_FAILED, new RuntimeException("DB error")));

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        // WHEN + THEN
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Basic " + basic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "amount": 100
                                }
                                """.formatted(src, dst)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("TRANSFER_FAILED"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void validateTransferShouldReturn200AndResult() throws Exception {
        UUID transferId = UUID.randomUUID();

        // Mock du résultat renvoyé par le service
        TransferValidationResult result = new TransferValidationResult(
                transferId,
                com.coreservice.api.dto.OperationStatus.COMPLETED
        );

        Mockito.when(transferService.validateTransfer(Mockito.any()))
               .thenReturn(result);

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());       

        // Appel du contrôleur
        mockMvc.perform(MockMvcRequestBuilders.put("/api/transfers")
                        .header("Authorization", "Basic " + basic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "transferId": "%s",
                              "decision": "APPROVE"
                            }
                        """.formatted(transferId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId").value(transferId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void validateTransferShouldReturn200AndResultRejected() throws Exception {
        UUID transferId = UUID.randomUUID();

        // Mock du résultat renvoyé par le service
        TransferValidationResult result = new TransferValidationResult(
                transferId,
                com.coreservice.api.dto.OperationStatus.FAILED
        );

        Mockito.when(transferService.validateTransfer(Mockito.any()))
               .thenReturn(result);

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());       

        // Appel du contrôleur
        mockMvc.perform(MockMvcRequestBuilders.put("/api/transfers")
                        .header("Authorization", "Basic " + basic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "transferId": "%s",
                              "decision": "REJECT"
                            }
                        """.formatted(transferId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId").value(transferId.toString()))
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void validateTransferShouldReturn400WhenTransferNotFound() throws Exception {
        UUID transferId = UUID.randomUUID();

        Mockito.when(transferService.validateTransfer(Mockito.any()))
            .thenThrow(new BusinessException(
                    BusinessError.TRANSFER_NOT_FOUND,
                    transferId.toString()
            ));

        String basic = Base64.getEncoder().encodeToString("user:password".getBytes());

        mockMvc.perform(put("/api/transfers")
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "transferId": "%s",
                          "decision": "APPROVE"
                        }
                    """.formatted(transferId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("TRANSFER_NOT_FOUND"))
            .andExpect(jsonPath("$.message", Matchers.containsString(transferId.toString())));
    }

}
