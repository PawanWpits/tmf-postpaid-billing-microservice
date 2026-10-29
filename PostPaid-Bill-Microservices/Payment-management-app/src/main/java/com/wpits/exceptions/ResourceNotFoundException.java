package com.wpits.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message, String baseType, String schemaLocation) {
        super(HttpStatus.NOT_FOUND, "404", "NOT_FOUND", message, baseType, schemaLocation);
    }
}
