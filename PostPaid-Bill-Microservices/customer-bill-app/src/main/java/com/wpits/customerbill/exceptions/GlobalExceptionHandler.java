package com.wpits.customerbill.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wpits.customerbill.dtos.ErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

	 @ExceptionHandler(ApiException.class)
	    public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex) {

		 ErrorResponseDto response = ErrorResponseDto.builder()
	                .type(ex.getClass().getSimpleName())
	                .code(ex.getCode())
	                .reason(ex.getReason())
	                .baseType("Error")
	                .schemaLocation("")
	                .message(ex.getMessage())
	                .status(String.valueOf(ex.getHttpStatus().value()))
	                .referenceError(ex.getReferenceError())
	                .build();

	        return ResponseEntity
	                .status(ex.getHttpStatus())
	                .body(response);
	    }
}
