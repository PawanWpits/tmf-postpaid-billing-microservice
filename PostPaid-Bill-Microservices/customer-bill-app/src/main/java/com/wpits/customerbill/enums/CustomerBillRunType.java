package com.wpits.customerbill.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum CustomerBillRunType {
    ON_CYCLE("onCycle"),
    OFF_CYCLE("offCycle");

    private final String value;

    CustomerBillRunType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CustomerBillRunType fromValue(String value) {
        if (value == null) return null;
        for (CustomerBillRunType t : values()) {
            if (t.value.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerBillRunType: " + value);
    }
}
