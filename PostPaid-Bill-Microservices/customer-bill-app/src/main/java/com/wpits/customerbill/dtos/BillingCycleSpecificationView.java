package com.wpits.customerbill.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillingCycleSpecificationView {
    private String id;
    private String href;
    private String name;
    private Integer billingDateShift;
    private String billingPeriod;
    private Integer chargeDateOffset;
    private Integer creditDateOffset;
    private Integer mailingDateOffset;
    private Integer paymentDueDateOffset;
    private String frequency;
}
