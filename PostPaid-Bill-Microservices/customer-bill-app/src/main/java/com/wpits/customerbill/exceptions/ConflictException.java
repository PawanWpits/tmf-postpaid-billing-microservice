package com.wpits.customerbill.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String message, String baseType, String schemaLocation) {
        super(HttpStatus.CONFLICT, "409", "CONFLICT", message, baseType, schemaLocation);
    }
}
