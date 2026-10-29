package com.wpits.customerbill.controllers;

import com.wpits.customerbill.dtos.CustomerBillDto;
import com.wpits.customerbill.dtos.CustomerBillPatchDto;
import com.wpits.customerbill.dtos.MoneyDto;
import com.wpits.customerbill.service.CustomerBillService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/customerBill")
@RequiredArgsConstructor
public class CustomerBillController {

    private final CustomerBillService customerBillService;

    @PostMapping
    public ResponseEntity<CustomerBillDto> create(@RequestBody CustomerBillDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerBillService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<JsonNode>> list(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.status(HttpStatus.OK).body(customerBillService.list(fields, offset, limit));
    }

    @GetMapping("/byBillDate")
    public ResponseEntity<List<CustomerBillDto>> getAllByBillDate(
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(customerBillService.getAllByBillDate(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> getById(@PathVariable @NotBlank String id,
                                             @RequestParam(required = false) String fields) {
        return ResponseEntity.status(HttpStatus.OK).body(customerBillService.getById(id, fields));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerBillDto> patch(@PathVariable @NotBlank String id,
                                                  @RequestBody CustomerBillPatchDto patch) {
        return ResponseEntity.status(HttpStatus.OK).body(customerBillService.patch(id, patch));
    }

    @PatchMapping("/{id}/remainingAmount")
    public ResponseEntity<CustomerBillDto> patchRemainingAmount(@PathVariable @NotBlank String id, @RequestBody MoneyDto remainingAmount) {
        return ResponseEntity.ok(customerBillService.patchRemainingAmt(id, remainingAmount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotBlank String id) {
        customerBillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
