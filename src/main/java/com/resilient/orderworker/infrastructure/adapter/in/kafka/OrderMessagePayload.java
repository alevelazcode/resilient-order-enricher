/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.kafka;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.resilient.orderworker.application.command.ProcessOrderCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record OrderMessagePayload(
        @JsonProperty("orderId") @NotBlank String orderId,
        @JsonProperty("customerId") @NotBlank String customerId,
        @JsonProperty("products") @NotEmpty @Valid List<LinePayload> products) {

    public ProcessOrderCommand toCommand() {
        return new ProcessOrderCommand(
                orderId,
                customerId,
                products.stream()
                        .map(p -> new ProcessOrderCommand.Line(p.productId(), p.quantity()))
                        .toList());
    }

    public record LinePayload(
            @JsonProperty("productId") @NotBlank String productId,
            @JsonProperty("quantity") @Positive int quantity) {}
}
