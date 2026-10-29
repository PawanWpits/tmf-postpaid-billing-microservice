package com.wpits.customerbill.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BillingPeriod {

    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
}
