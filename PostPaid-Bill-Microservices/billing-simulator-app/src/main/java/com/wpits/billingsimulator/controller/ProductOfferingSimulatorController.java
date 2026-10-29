package com.wpits.billingsimulator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/productOrdering/v4/productOrder")
public class ProductOfferingSimulatorController {

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductOffering(@PathVariable String id) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", id);
        response.put("href", "http://localhost:8082/productOrdering/v4/productOrder/" + id);
        response.put("name", "Sample Product Offering");
        response.put("state", "NEW");
        response.put("amount", money("100.00"));
        response.put("taxAmount", money("10.50"));
        response.put("totalAmount", money("110.50"));
        response.put("remainingAmount", money("110.50"));
        response.put("@type", "ProductOffering");

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> money(String value) {
        Map<String, Object> amount = new LinkedHashMap<>();
        amount.put("unit", "INR");
        amount.put("value", new BigDecimal(value));
        return amount;
    }
}
