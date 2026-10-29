package com.wpits.accountmanagement.exceptions;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String code;
    private final String reason;
    private final String referenceError;
    private final String baseType;
    private final String schemaLocation;

    protected ApiException(HttpStatus httpStatus,
                            String code,
                            String reason,
                            String message,
                            String baseType,
                            String schemaLocation) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.reason = reason;
        this.baseType = baseType;
        this.schemaLocation = schemaLocation;
        this.referenceError = UUID.randomUUID().toString();
    }
}
