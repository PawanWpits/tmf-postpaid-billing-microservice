package com.wpits.billingsimulator.controller;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductOfferingSimulatorControllerTest {

    @Test
    void returnsProductOfferingPaymentAmountsAsStructuredJsonFields() {
        ProductOfferingSimulatorController controller = new ProductOfferingSimulatorController();

        Map<String, Object> response = controller.getProductOffering("PO-001").getBody();

        assertEquals("PO-001", response.get("id"));
        assertEquals("ProductOffering", response.get("@type"));
        assertEquals(new BigDecimal("599.00"), ((Map<?, ?>) response.get("amount")).get("value"));
        assertEquals(new BigDecimal("59.90"), ((Map<?, ?>) response.get("taxAmount")).get("value"));
        assertEquals(new BigDecimal("658.90"), ((Map<?, ?>) response.get("totalAmount")).get("value"));
        assertEquals(new BigDecimal("658.90"), ((Map<?, ?>) response.get("remainingAmount")).get("value"));
    }
}
