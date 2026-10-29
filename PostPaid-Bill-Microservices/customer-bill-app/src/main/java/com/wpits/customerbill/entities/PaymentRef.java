package com.wpits.customerbill.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRef {
    private String id;
    private String href;
    private String name;
    private String referredType;
    private String baseType;
    private String schemaLocation;
    private String type;
}
