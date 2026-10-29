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
public class ContactDto {
    private String id;
    private String contactName;
    private String contactType;
    private String partyRoleType;
    private Boolean preferred;

    /** "email" | "phone" | "fax" | "social" | "geographicAddress" */
    private String mediumType;
    private String emailAddress;
    private String phoneNumber;
    private String faxNumber;
    private String socialNetworkId;
    private String street1;
    private String street2;
    private String city;
    private String stateOrProvince;
    private String postCode;
    private String country;

    private TimePeriodDto validFor;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@type")
    private String type;
}
