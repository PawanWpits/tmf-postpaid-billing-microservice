package com.wpits.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItem {
    @Id
    private String id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "amount_value"))
    })
    private Money amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "tax_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "tax_amount_value"))
    })
    private Money taxAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "total_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "total_amount_value"))
    })
    private Money totalAmount;

    private String baseType;
    private String schemaLocation;
    private String type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "item_id", nullable = false)
    private EntityRef item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
}
