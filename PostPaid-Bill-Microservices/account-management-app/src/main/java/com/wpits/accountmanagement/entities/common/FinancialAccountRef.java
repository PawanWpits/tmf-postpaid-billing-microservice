package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 FinancialAccountRef - lightweight reference to a FinancialAccount (AccountReceivable reference). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAccountRef {
    private String id;
    private String href;
    private String name;
    private String referredType;
}
