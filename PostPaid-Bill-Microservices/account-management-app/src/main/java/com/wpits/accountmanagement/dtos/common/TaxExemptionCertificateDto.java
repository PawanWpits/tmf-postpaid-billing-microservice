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
public class TaxExemptionCertificateDto {
    private String id;
    private String certificateNumber;
    private String issuingJurisdiction;
    private String reason;
    private String attachmentName;
    private String attachmentUrl;
    private TaxDefinitionDto taxDefinition;
    private TimePeriodDto validFor;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@type")
    private String type;
}
