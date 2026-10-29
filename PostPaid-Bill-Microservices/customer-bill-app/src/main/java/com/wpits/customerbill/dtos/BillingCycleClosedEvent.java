package com.wpits.customerbill.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingCycleClosedEvent {
    private String eventType;
    private String billingAccountId;
    private String billingCycleSpecificationId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate periodStart;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate periodEnd;
}
