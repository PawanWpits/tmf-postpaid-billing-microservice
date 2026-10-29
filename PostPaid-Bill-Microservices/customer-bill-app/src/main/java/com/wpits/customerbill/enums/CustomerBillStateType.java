package com.wpits.customerbill.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum CustomerBillStateType {
    NEW("new"),
    VALIDATED("validated"),
    SENT("sent"),
    SETTLED("settled"),
    PARTIALLY_PAID("partiallyPaid"),
    ON_HOLD("onHold");

    private final String value;

    CustomerBillStateType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CustomerBillStateType fromValue(String value) {
        if (value == null) return null;
        for (CustomerBillStateType t : values()) {
            if (t.value.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerBillStateType: " + value);
    }
}
