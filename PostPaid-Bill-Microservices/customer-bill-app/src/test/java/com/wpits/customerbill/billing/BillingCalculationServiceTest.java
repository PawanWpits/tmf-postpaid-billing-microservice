package com.wpits.customerbill.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingCalculationServiceTest {

    @Test
    void calculatesSubtotalTaxAndAmountDueUsingBigDecimal() {
        BillingCalculationService service = new BillingCalculationService();

        BillCalculation result = service.calculate(new BillingInput(
                new BigDecimal("599.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("18"),"INR"));

        assertEquals(new BigDecimal("699.00"), result.subtotal());
        assertEquals(new BigDecimal("125.82"), result.tax());
        assertEquals(new BigDecimal("824.82"), result.currentBill());
        assertEquals(new BigDecimal("1024.82"), result.amountDue());
    }
}
