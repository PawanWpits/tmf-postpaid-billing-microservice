package com.wpits.controllers;

import com.wpits.dtos.PartialPaymentRequest;
import com.wpits.service.PartialPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/partial/paymentManagement/v4/payment")
@RequiredArgsConstructor
public class PartialPaymentController {

    private final PartialPaymentService partialPaymentService;

    @PostMapping
    public ResponseEntity<String> processPartialPayment(@RequestBody PartialPaymentRequest request) {

        return ResponseEntity.ok(partialPaymentService.processPayment(request));
    }
}
