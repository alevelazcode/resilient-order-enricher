/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.resilient.orderworker.domain.order.Order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Enriched order with customer and product details")
public record OrderResponse(
        @JsonProperty("orderId") String orderId,
        @JsonProperty("customerId") String customerId,
        @JsonProperty("customerName") String customerName,
        @JsonProperty("customerStatus") String customerStatus,
        @JsonProperty("lines") List<OrderLineResponse> lines,
        @JsonProperty("totalAmount") BigDecimal totalAmount,
        @JsonProperty("processedAt") Instant processedAt,
        @JsonProperty("status") String status) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.orderId(),
                order.customerId(),
                order.customerName(),
                order.customerStatus(),
                order.lines().stream().map(OrderLineResponse::from).toList(),
                order.totalAmount(),
                order.processedAt(),
                order.status().name());
    }
}
