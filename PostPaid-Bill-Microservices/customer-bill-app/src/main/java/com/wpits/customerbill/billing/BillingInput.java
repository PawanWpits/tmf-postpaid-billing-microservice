package com.wpits.customerbill.billing;

import java.math.BigDecimal;

public class BillingInput {

    private final String planName;
    private final BigDecimal monthlyPlanCharge;
    private final BigDecimal usageCharge;
    private final BigDecimal vasCharge;
    private final BigDecimal discount;
    private final BigDecimal previousBalance;
    private final BigDecimal taxRate;
    private final String currency;

    public BillingInput(BigDecimal monthlyPlanCharge, BigDecimal usageCharge, BigDecimal vasCharge,
                        BigDecimal discount, BigDecimal previousBalance, BigDecimal taxRate,
                        String currency) {
        this(null, monthlyPlanCharge, usageCharge, vasCharge, discount, previousBalance, taxRate, currency);
    }

    public BillingInput(String planName, BigDecimal monthlyPlanCharge, BigDecimal usageCharge, BigDecimal vasCharge,
                        BigDecimal discount, BigDecimal previousBalance, BigDecimal taxRate,
                        String currency) {
        this.planName = planName;
        this.monthlyPlanCharge = monthlyPlanCharge;
        this.usageCharge = usageCharge;
        this.vasCharge = vasCharge;
        this.discount = discount;
        this.previousBalance = previousBalance;
        this.taxRate = taxRate;
        this.currency = currency;
    }

    public String planName() { return planName; }
    public BigDecimal monthlyPlanCharge() { return monthlyPlanCharge; }
    public BigDecimal usageCharge() { return usageCharge; }
    public BigDecimal vasCharge() { return vasCharge; }
    public BigDecimal discount() { return discount; }
    public BigDecimal previousBalance() { return previousBalance; }
    public BigDecimal taxRate() { return taxRate; }
    public String currency() { return currency; }
}
