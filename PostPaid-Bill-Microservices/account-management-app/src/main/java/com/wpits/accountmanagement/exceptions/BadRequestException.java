package com.wpits.accountmanagement.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String message, String baseType, String schemaLocation) {
        super(HttpStatus.BAD_REQUEST, "400", "BAD_REQUEST", message, baseType, schemaLocation);
    }
}
