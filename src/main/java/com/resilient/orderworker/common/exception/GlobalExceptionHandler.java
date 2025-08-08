/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.common.exception;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import reactor.core.publisher.Mono;

/**
 * Global exception handler for the application. Provides centralized error handling and logging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleOrderNotFound(OrderNotFoundException ex) {
        LOGGER.error("Order not found: {}", ex.getMessage());
        ErrorResponse error =
                new ErrorResponse(
                        "ORDER_NOT_FOUND",
                        ex.getMessage(),
                        LocalDateTime.now(ZoneId.systemDefault()));
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomerNotFound(
            CustomerNotFoundException ex) {
        LOGGER.error("Customer not found: {}", ex.getMessage());
        ErrorResponse error =
                new ErrorResponse(
                        "CUSTOMER_NOT_FOUND",
                        ex.getMessage(),
                        LocalDateTime.now(ZoneId.systemDefault()));
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleProductNotFound(ProductNotFoundException ex) {
        LOGGER.error("Product not found: {}", ex.getMessage());
        ErrorResponse error =
                new ErrorResponse(
                        "PRODUCT_NOT_FOUND",
                        ex.getMessage(),
                        LocalDateTime.now(ZoneId.systemDefault()));
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(OrderProcessingException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleOrderProcessing(OrderProcessingException ex) {
        LOGGER.error("Order processing error: {}", ex.getMessage());
        ErrorResponse error =
                new ErrorResponse(
                        "ORDER_PROCESSING_ERROR",
                        ex.getMessage(),
                        LocalDateTime.now(ZoneId.systemDefault()));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(ExternalApiException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleExternalApi(ExternalApiException ex) {
        LOGGER.error("External API error: {}", ex.getMessage());
        ErrorResponse error =
                new ErrorResponse(
                        "EXTERNAL_API_ERROR",
                        ex.getMessage(),
                        LocalDateTime.now(ZoneId.systemDefault()));
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(WebExchangeBindException ex) {
        LOGGER.error("Validation error: {}", ex.getMessage());

        List<String> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());

        ErrorResponse error =
                new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Validation failed: " + String.join(", ", errors),
                        LocalDateTime.now(ZoneId.systemDefault()));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneral(Exception ex) {
        LOGGER.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponse error =
                new ErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred",
                        LocalDateTime.now(ZoneId.systemDefault()));
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    /** Standard error response format. */
    public record ErrorResponse(String code, String message, LocalDateTime timestamp) {}
}
