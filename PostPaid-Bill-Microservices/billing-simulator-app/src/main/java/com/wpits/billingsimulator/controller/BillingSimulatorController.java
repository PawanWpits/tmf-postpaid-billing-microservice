package com.wpits.billingsimulator.controller;

import com.wpits.billingsimulator.dto.ChargesResponse;
import com.wpits.billingsimulator.service.ChargeDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BillingSimulatorController {

    private final ChargeDetailService service;

    @GetMapping("/charges")
    public ResponseEntity<ChargesResponse> getCharges(
            @RequestParam String billingAccountId,
            @RequestParam LocalDate periodStart,
            @RequestParam LocalDate periodEnd) {

        return ResponseEntity.ok(service.chargeDetails(billingAccountId,periodStart,periodEnd));
    }
}
