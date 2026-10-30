package com.wpits.billingsimulator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tmf-api/paymentManagement/v5/payments")
public class PaymentGatewaySimulator {

    @PostMapping
    public ResponseEntity<Map<String, String>> PaymentGatewaySimulator(@RequestBody Map<String, Object> paymentRequest) {

        System.out.println(paymentRequest);
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "message", "Payment processed successfully"
                )
        );

    }
}
