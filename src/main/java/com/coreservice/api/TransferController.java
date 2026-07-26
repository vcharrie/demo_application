package com.coreservice.api;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coreservice.api.dto.TransferRequest;
import com.coreservice.api.dto.TransferResult;
import com.coreservice.api.mapper.TransferApiMapper;
import com.coreservice.application.TransferService;
import com.coreservice.domain.Transfer;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResult> initiateTransfer(@RequestBody @Valid TransferRequest request) {   
        // Mapping DTO → domaine
        UUID sourceId = request.sourceAccountId();
        UUID destinationId = request.destinationAccountId();
        BigDecimal amount = request.amount();

        // Appel du service métier
        Transfer transfer = transferService.initiateTransfer(sourceId, destinationId, amount);

        // Mapping domaine → DTO
        TransferResult result = TransferApiMapper.toTransferResult(transfer);

        
        return ResponseEntity.ok(result);
    }
}