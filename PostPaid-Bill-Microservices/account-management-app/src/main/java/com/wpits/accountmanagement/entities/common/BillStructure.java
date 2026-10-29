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
 * TMF666 BillStructure sub-resource - PartyAccount/BillingAccount/SettlementAccount.billStructure (0..1).
 * All three nested refs share the same id/href/name/referredType shape, so
 * each is remapped to a unique column prefix to avoid collisions.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillStructure {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "cycle_spec_id")),
            @AttributeOverride(name = "href", column = @Column(name = "cycle_spec_href")),
            @AttributeOverride(name = "name", column = @Column(name = "cycle_spec_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "cycle_spec_referred_type"))
    })
    private BillingCycleSpecificationRef cycleSpecification;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "bill_format_id")),
            @AttributeOverride(name = "href", column = @Column(name = "bill_format_href")),
            @AttributeOverride(name = "name", column = @Column(name = "bill_format_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "bill_format_referred_type"))
    })
    private BillFormatRef format;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "presentation_media_id")),
            @AttributeOverride(name = "href", column = @Column(name = "presentation_media_href")),
            @AttributeOverride(name = "name", column = @Column(name = "presentation_media_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "presentation_media_referred_type"))
    })
    private BillPresentationMediaRef presentationMedia;
}
