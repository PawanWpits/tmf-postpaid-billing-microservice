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

/**
 * TMF666 TaxExemptionCertificate sub-resource - Account.taxExemption (0..*).
 * NOTE: {@code attachment} (AttachmentRefOrValue in the spec) is simplified to
 * a plain name/url pair here to avoid a third level of embeddable nesting.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxExemptionCertificate {

    /** Identifier of the exemption within the list. */
    private String id;

    private String certificateNumber;
    private String issuingJurisdiction;
    private String reason;

    private String attachmentName;
    private String attachmentUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "tax_definition_id")),
            @AttributeOverride(name = "jurisdictionLevel", column = @Column(name = "tax_jurisdiction_level")),
            @AttributeOverride(name = "jurisdictionName", column = @Column(name = "tax_jurisdiction_name")),
            @AttributeOverride(name = "name", column = @Column(name = "tax_definition_name")),
            @AttributeOverride(name = "taxType", column = @Column(name = "tax_type")),
            @AttributeOverride(name = "validFor.startDateTime", column = @Column(name = "tax_definition_valid_from")),
            @AttributeOverride(name = "validFor.endDateTime", column = @Column(name = "tax_definition_valid_to"))
    })
    private TaxDefinition taxDefinition;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "exemption_valid_from")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "exemption_valid_to"))
    })
    private TimePeriod validFor;
}
