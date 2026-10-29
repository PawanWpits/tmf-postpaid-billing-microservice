package com.wpits.accountmanagement.entities;

import com.wpits.accountmanagement.entities.common.TimePeriod;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMF666 BillingCycleSpecification managed entity (spec page 80) - a detailed
 * description of when to initiate a billing cycle and its various sub-steps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "billing_cycle_specification")
public class BillingCycleSpecification {

    @Id
    private String id;

    private String href;
    private String name;
    private String description;

    /** offset in days, relative to the start of the billing/settlement period */
    private Integer billingDateShift;

    /** e.g. week, month, quarter, year */
    private String billingPeriod;

    private Integer chargeDateOffset;
    private Integer creditDateOffset;
    private Integer mailingDateOffset;
    private Integer paymentDueDateOffset;

    /** e.g. monthly */
    private String frequency;

    @Embedded
    private TimePeriod validFor;

    private String baseType;
    private String schemaLocation;
    private String type;
}
