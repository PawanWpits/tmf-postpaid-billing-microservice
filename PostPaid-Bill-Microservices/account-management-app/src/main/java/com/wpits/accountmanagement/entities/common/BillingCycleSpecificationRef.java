package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 BillingCycleSpecificationRef - lightweight reference to a BillingCycleSpecification (when to initiate a billing cycle). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingCycleSpecificationRef {
    private String id;
    private String href;
    private String name;
    private String referredType;
}
