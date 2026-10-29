package com.wpits.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRefOrValueDto {
    private String id;
    private String description;
    private String href;

    @JsonProperty("isPreferred")
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean preferred = Boolean.FALSE;
    private String name;
    private String status;
    private LocalDateTime statusDate;
    private TimePeriodDto validFor;
    private RelatedPartyDto relatedParty;
    private List<AccountRefDto> account;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@referredType")
    private String referredType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}
