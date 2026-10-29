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
public class FinancialAccountRefDto {

    private String id;
    private String href;
    private String name;

    @JsonProperty("@referredType")
    private String referredType;

    @JsonProperty("@type")
    private String type;
}
