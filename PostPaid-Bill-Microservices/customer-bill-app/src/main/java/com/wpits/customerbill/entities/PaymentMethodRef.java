package com.wpits.customerbill.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_method_ref")
public class PaymentMethodRef {
    @Id
    private String id;
    private String href;
    private String name;
    private String referredType;
    private String baseType;
    private String schemaLocation;
    private String type;
}
