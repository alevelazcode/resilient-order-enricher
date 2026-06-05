/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.rest;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response payload")
public record ErrorResponse(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("timestamp") Instant timestamp) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Instant.now());
    }
}
