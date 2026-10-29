package com.wpits.accountmanagement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * TMF666 Account.state - "Contains the lifecycle state such as: Active,
 * Closed, Suspended and so on." The spec deliberately types this as a plain
 * String (implementations are free to use their own enumeration); this is
 * the enumeration used by this implementation for PartyAccount,
 * BillingAccount, FinancialAccount and SettlementAccount.
 */
public enum AccountStateType {
    PENDING_ACTIVE("PendingActive"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    CLOSED("Closed");

    private final String value;

    AccountStateType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccountStateType fromValue(String value) {
        if (value == null) return null;
        for (AccountStateType t : values()) {
            if (t.value.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown AccountStateType: " + value);
    }
}
