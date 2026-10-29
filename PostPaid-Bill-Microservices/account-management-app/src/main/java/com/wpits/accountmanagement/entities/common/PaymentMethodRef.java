package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 PaymentMethodRef - lightweight reference to a PaymentMethod (a specific mean of payment (e.g direct debit)). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRef {
    private String id;
    private String href;
    private String name;
    private String referredType;
}
