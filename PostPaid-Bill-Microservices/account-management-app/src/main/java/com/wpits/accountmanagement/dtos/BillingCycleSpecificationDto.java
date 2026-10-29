package com.wpits.accountmanagement.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wpits.accountmanagement.dtos.common.TimePeriodDto;
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
public class BillingCycleSpecificationDto {
    private String id;
    private String href;

    private String name;
    private String description;

    private Integer billingDateShift;
    private String billingPeriod;
    private Integer chargeDateOffset;
    private Integer creditDateOffset;
    private Integer mailingDateOffset;
    private Integer paymentDueDateOffset;
    private String frequency;
    private TimePeriodDto validFor;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}
