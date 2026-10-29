package com.wpits.customerbill.dtos;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillCycleDto {

    private String id;
    private String href;
    private String name;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime billingDate;

    private String billingPeriod;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime chargeDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime creditDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime mailingDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime paymentDueDate;

    private BillCycleSpecificationRefDto billCycleSpecification;

    private TimePeriodDto validFor;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}