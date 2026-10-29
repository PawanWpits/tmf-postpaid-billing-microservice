package com.wpits.customerbill.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tax_item")
public class TaxItem {

    @Id
    private String id;

    private String taxCategory;
    private BigDecimal taxRate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "tax_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "tax_amount_value"))
    })
    private Money taxAmount;

    private String baseType;
    private String schemaLocation;

    @Builder.Default
    private String type = "TaxItem";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_bill_id")
    private CustomerBill customerBill;
}
