package com.wpits.customerbill.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerBillOnDemandDto {

    private String id;
    private String href;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime lastUpdate;

    private String name;
    private String state;
    private BillingAccountRefDto billingAccount;
    private CustomerBillRefDto customerBill;
    private RelatedPartyRefOrPartyRoleRefDto relatedParty;
    private String documentStatus;
    private String documentPath;
    private String errorMessage;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
}
