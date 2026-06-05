/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.rest;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.resilient.orderworker.domain.order.OrderLine;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Line item within an order")
public record OrderLineResponse(
        @JsonProperty("productId") String productId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("unitPrice") BigDecimal unitPrice,
        @JsonProperty("quantity") int quantity,
        @JsonProperty("subtotal") BigDecimal subtotal) {

    public static OrderLineResponse from(OrderLine line) {
        return new OrderLineResponse(
                line.productId(),
                line.name(),
                line.description(),
                line.unitPrice(),
                line.quantity(),
                line.subtotal());
    }
}
