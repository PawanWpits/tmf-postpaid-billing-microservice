package com.wpits.customerbill.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "applied_payment")
public class AppliedPayment {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "applied_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "applied_amount_value"))
    })
    private Money appliedAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "payment_id")),
            @AttributeOverride(name = "href", column = @Column(name = "payment_href")),
            @AttributeOverride(name = "name", column = @Column(name = "payment_name")),
            @AttributeOverride(name = "referredType", column = @Column(name = "payment_referred_type")),
            @AttributeOverride(name = "baseType", column = @Column(name = "payment_base_type")),
            @AttributeOverride(name = "schemaLocation", column = @Column(name = "payment_schema_location")),
            @AttributeOverride(name = "type", column = @Column(name = "payment_type"))
    })
    private PaymentRef payment;

    private String baseType;
    private String schemaLocation;

    @Builder.Default
    private String type = "AppliedPayment";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_bill_id")
    private CustomerBill customerBill;
}
