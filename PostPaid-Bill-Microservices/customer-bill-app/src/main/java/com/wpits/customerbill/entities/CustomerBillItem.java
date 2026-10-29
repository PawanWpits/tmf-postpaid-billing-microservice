package com.wpits.customerbill.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_bill_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBillItem {

    @Id
    private String id;

    private String description;
    private String planName;
    private BigDecimal amount;
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_bill_id", nullable = false)
    private CustomerBill customerBill;
}
