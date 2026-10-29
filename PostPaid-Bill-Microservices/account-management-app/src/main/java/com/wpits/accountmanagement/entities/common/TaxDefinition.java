package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 TaxDefinition sub-resource - nested inside TaxExemptionCertificate.taxDefinition. */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxDefinition {

    /** Unique identifier of the tax. */
    private String id;

    private String jurisdictionLevel;
    private String jurisdictionName;

    /** Tax name. */
    private String name;

    private String taxType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "tax_valid_from")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "tax_valid_to"))
    })
    private TimePeriod validFor;
}
