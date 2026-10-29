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

/** TMF666 AccountBalance sub-resource - Account.accountBalance (0..*). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {

    /** Identifier of the balance within the list of balances. */
    private String id;

    /** e.g. deposit balance, disputed balance, loyalty balance, receivable balance */
    private String balanceType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "unit", column = @Column(name = "balance_amount_unit")),
            @AttributeOverride(name = "value", column = @Column(name = "balance_amount_value"))
    })
    private Money amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "balance_valid_from")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "balance_valid_to"))
    })
    private TimePeriod validFor;
}
