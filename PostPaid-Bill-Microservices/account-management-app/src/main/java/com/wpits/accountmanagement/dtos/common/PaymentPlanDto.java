package com.wpits.accountmanagement.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentPlanDto {
    private String id;
    private Integer numberOfPayments;
    private String paymentFrequency;
    private String planType;
    private Integer priority;
    private String status;
    private MoneyDto totalAmount;
    private TimePeriodDto validFor;
    private PaymentMethodRefDto paymentMethod;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@type")
    private String type;
}
