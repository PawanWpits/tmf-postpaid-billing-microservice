package com.wpits.exceptions;

import com.wpits.dtos.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_TYPE = "Error";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex) {
        ErrorResponseDto response = ErrorResponseDto.builder()
                .type(ex.getClass().getSimpleName())
                .code(ex.getCode())
                .reason(ex.getReason())
                .baseType(ex.getBaseType())
                .schemaLocation(ex.getSchemaLocation())
                .message(ex.getMessage())
                .status(String.valueOf(ex.getHttpStatus().value()))
                .referenceError(ex.getReferenceError())
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() == null ? status.toString() : ex.getReason();
        return errorResponse(status, reasonFor(status), message, UUID.randomUUID().toString());
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponseDto> handleDownstreamHttpException(RestClientResponseException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = "Downstream service returned HTTP " + status.value();
        String referenceError = UUID.randomUUID().toString();

        try {
            JSONObject upstreamError = new JSONObject(ex.getResponseBodyAsString());
            String upstreamMessage = upstreamError.optString("message");
            if (!upstreamMessage.isBlank()) {
                message = upstreamMessage;
            }
            String upstreamReference = upstreamError.optString("referenceError");
            if (!upstreamReference.isBlank()) {
                referenceError = upstreamReference;
            }
        } catch (JSONException ignored) {
            log.debug("Downstream error response was not a TMF JSON error");
        }

        return errorResponse(status, reasonFor(status), message, referenceError);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponseDto> handleDownstreamConnectionException(ResourceAccessException ex) {
        log.warn("Unable to reach a downstream service", ex);
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE",
                "A required downstream service is unavailable", UUID.randomUUID().toString());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadableRequest(HttpMessageNotReadableException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Request body is missing or contains invalid JSON", UUID.randomUUID().toString());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error while handling payment request", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred", UUID.randomUUID().toString());
    }

    private ResponseEntity<ErrorResponseDto> errorResponse(HttpStatusCode status, String reason,
                                                            String message, String referenceError) {
        HttpStatus knownStatus = HttpStatus.resolve(status.value());
        ErrorResponseDto response = ErrorResponseDto.builder()
                .type(typeFor(status))
                .code(String.valueOf(status.value()))
                .reason(reason)
                .baseType(ERROR_BASE_TYPE)
                .schemaLocation("")
                .message(message)
                .status(String.valueOf(status.value()))
                .referenceError(referenceError)
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private String reasonFor(HttpStatusCode status) {
        HttpStatus knownStatus = HttpStatus.resolve(status.value());
        return knownStatus == null ? "HTTP_ERROR" : knownStatus.name();
    }

    private String typeFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> BadRequestException.class.getSimpleName();
            case 404 -> ResourceNotFoundException.class.getSimpleName();
            case 409 -> ConflictException.class.getSimpleName();
            default -> "ApiException";
        };
    }
}
