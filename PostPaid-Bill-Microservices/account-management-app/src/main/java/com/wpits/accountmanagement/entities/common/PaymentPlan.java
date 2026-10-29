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

/** TMF666 PaymentPlan sub-resource - Account.paymentPlan (0..*). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPlan {

    /** Identifier of the plan within the list. */
    private String id;

    private Integer numberOfPayments;
    private String paymentFrequency;
    private String planType;
    private Integer priority;

    /** e.g. effective, ineffective */
    private String status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "total_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "total_amount_value"))
    })
    private Money totalAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "plan_valid_from")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "plan_valid_to"))
    })
    private TimePeriod validFor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "payment_method_id")),
            @AttributeOverride(name = "href", column = @Column(name = "payment_method_href")),
            @AttributeOverride(name = "name", column = @Column(name = "payment_method_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "payment_method_referred_type"))
    })
    private PaymentMethodRef paymentMethod;
}
