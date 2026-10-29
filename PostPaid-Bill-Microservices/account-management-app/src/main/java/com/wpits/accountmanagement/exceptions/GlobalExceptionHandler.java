package com.wpits.accountmanagement.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handling for the whole Account Management API.
 * Every error response follows the TMF "Error" representation shape.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex) {
        log.warn("API exception [{}]: {}", ex.getCode(), ex.getMessage());

        ErrorResponseDto response = ErrorResponseDto.builder()
                .type(ex.getClass().getSimpleName())
                .code(ex.getCode())
                .reason(ex.getReason())
                .baseType("Error")
                .schemaLocation(ex.getSchemaLocation())
                .message(ex.getMessage())
                .status(String.valueOf(ex.getHttpStatus().value()))
                .referenceError(ex.getReferenceError())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponseDto response = ErrorResponseDto.builder()
                .type("ValidationException")
                .code("400")
                .reason("BAD_REQUEST")
                .baseType("Error")
                .message("Validation failed: " + fieldErrors)
                .status(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .referenceError(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponseDto response = ErrorResponseDto.builder()
                .type("BadRequestException")
                .code("400")
                .reason("BAD_REQUEST")
                .baseType("Error")
                .message(ex.getMessage())
                .status(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .referenceError(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponseDto response = ErrorResponseDto.builder()
                .type("InternalServerError")
                .code("500")
                .reason("INTERNAL_SERVER_ERROR")
                .baseType("Error")
                .message("An unexpected error occurred. Please contact support.")
                .status(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .referenceError(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
