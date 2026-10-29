package com.wpits.billingsimulator.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargesResponse {
    private String billingAccountId;
    private String periodStart;
    private String periodEnd;
    private String planName;
    private BigDecimal monthlyPlanCharge;
    private BigDecimal usageCharge;
    private BigDecimal vasCharge;
    private BigDecimal discount;
    private BigDecimal previousBalance;
    private BigDecimal taxRate;
    private String currency;
}
