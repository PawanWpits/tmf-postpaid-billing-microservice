package com.wpits.customerbill.billing;

import java.math.BigDecimal;

public class BillCalculation {
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal currentBill;
    private final BigDecimal amountDue;

    public BillCalculation(BigDecimal subtotal, BigDecimal tax, BigDecimal currentBill, BigDecimal amountDue) {
        this.subtotal = subtotal;
        this.tax = tax;
        this.currentBill = currentBill;
        this.amountDue = amountDue;
    }

    public BigDecimal subtotal() { return subtotal; }
    public BigDecimal tax() { return tax; }
    public BigDecimal currentBill() { return currentBill; }
    public BigDecimal amountDue() { return amountDue; }
}
