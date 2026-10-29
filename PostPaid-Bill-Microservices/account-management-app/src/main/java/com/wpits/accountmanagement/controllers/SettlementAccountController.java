package com.wpits.accountmanagement.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.wpits.accountmanagement.dtos.SettlementAccountDto;
import com.wpits.accountmanagement.service.SettlementAccountService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/settlementAccount")
@RequiredArgsConstructor
public class SettlementAccountController {

    private final SettlementAccountService service;

    @GetMapping
    public ResponseEntity<List<JsonNode>> list(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.status(HttpStatus.OK).body(service.list(fields, offset, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> getById(@PathVariable @NotBlank String id,
                                             @RequestParam(required = false) String fields) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getById(id, fields));
    }

    @PostMapping
    public ResponseEntity<SettlementAccountDto> create(@RequestBody SettlementAccountDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SettlementAccountDto> patch(@PathVariable @NotBlank String id,
                                                        @RequestBody SettlementAccountDto patch) {
        return ResponseEntity.status(HttpStatus.OK).body(service.patch(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotBlank String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
