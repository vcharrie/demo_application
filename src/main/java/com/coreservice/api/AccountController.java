package com.coreservice.api;

import com.coreservice.api.dto.AccountRequest;
import com.coreservice.api.dto.AccountResponse;
import com.coreservice.api.dto.DepositRequest;
import com.coreservice.api.dto.WithdrawRequest;
import com.coreservice.api.mapper.AccountApiMapper;
import com.coreservice.application.AccountService;
import com.coreservice.domain.Account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        var created = service.createAccount(
                request.ownerId(),
                request.initialBalance()
        );
        return ResponseEntity.status(201).body(AccountApiMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll() {
        var accounts = service.findAll()
                .stream()
                .map(AccountApiMapper::toResponse)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(
            @PathVariable
            @Pattern(regexp = "^[a-fA-F0-9-]{36}$")
            String id) {

        Account account = service.findById(UUID.fromString(id));
        return ResponseEntity.ok(AccountApiMapper.toResponse(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable
            @Pattern(regexp = "^[a-fA-F0-9-]{36}$")
            String id) {

        service.delete(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposit")
    public Account deposit(
            @PathVariable UUID id,
            @Valid @RequestBody DepositRequest request) {

        return service.deposit(id, request.amount());
    } 

    @PostMapping("/{id}/withdraw")
    public Account withdraw(
        @PathVariable UUID id,
        @Valid @RequestBody WithdrawRequest request) {

        return service.withdraw(id, request.amount());
    }
    

}