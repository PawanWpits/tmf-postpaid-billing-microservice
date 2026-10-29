package com.wpits.customerbill.dtos;

import com.fasterxml.jackson.annotation.*;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillCycleSpecificationRefDto {

    private String id;

    private String href;

    private String name;

    private String description;

    @JsonProperty("@referredType")
    private String referredType;

    @JsonProperty("@baseType")
    private String baseType;

    @JsonProperty("@schemaLocation")
    private String schemaLocation;

    @JsonProperty("@type")
    private String type;
    
}