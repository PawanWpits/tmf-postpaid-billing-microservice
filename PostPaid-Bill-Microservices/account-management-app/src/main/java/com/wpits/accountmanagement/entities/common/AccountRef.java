package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TMF666 AccountRef - lightweight reference to a Account (account may be a party account or a financial account). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRef {
    private String id;
    private String href;
    private String name;
    private String referredType;
}
