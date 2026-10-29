package com.wpits.customerbill.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CustomerBillOnDemandStateType {
    IN_PROGRESS("inProgress"),
    REJECTED("rejected"),
    DONE("done"),
    TERMINATED_WITH_ERROR("terminatedWithError");

    private final String value;

    CustomerBillOnDemandStateType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CustomerBillOnDemandStateType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CustomerBillOnDemandStateType state : values()) {
            if (state.value.equalsIgnoreCase(value) || state.name().equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerBillOnDemandStateType: " + value);
    }
}
