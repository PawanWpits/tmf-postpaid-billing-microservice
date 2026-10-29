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
public class TaxDefinitionDto {
    private String id;
    private String jurisdictionLevel;
    private String jurisdictionName;
    private String name;
    private String taxType;
    private TimePeriodDto validFor;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@type")
    private String type;
}
