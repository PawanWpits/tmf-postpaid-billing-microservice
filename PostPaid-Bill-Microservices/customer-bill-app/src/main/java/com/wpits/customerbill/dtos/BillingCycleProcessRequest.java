package com.wpits.customerbill.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingCycleProcessRequest {

    private String billingAccountId;
    private String billingCycleSpecificationId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate periodStart;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate periodEnd;
}
