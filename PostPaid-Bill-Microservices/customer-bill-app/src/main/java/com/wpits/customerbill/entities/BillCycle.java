package com.wpits.customerbill.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bill_cycle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillCycle {

    @Id
    private String id;

    private String href;

    private String name;

    private String description;

    private LocalDateTime billingDate;

    private String billingPeriod;

    private LocalDateTime chargeDate;

    private LocalDateTime creditDate;

    private LocalDateTime mailingDate;

    private LocalDateTime paymentDueDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="bill_cycle_spec_id")
    private BillCycleSpecificationRef billCycleSpecification;

    @Embedded
    private TimePeriod validFor;

    private String baseType;

    private String schemaLocation;

    @Builder.Default
    private String type = "BillCycle";
}