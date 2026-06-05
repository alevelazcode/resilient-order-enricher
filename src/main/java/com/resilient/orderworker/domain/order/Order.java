/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record Order(
        String orderId,
        String customerId,
        String customerName,
        String customerStatus,
        List<OrderLine> lines,
        BigDecimal totalAmount,
        Instant processedAt,
        OrderStatus status) {

    public Order {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(lines, "lines");
        Objects.requireNonNull(totalAmount, "totalAmount");
        Objects.requireNonNull(processedAt, "processedAt");
        Objects.requireNonNull(status, "status");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one line");
        }
        lines = List.copyOf(lines);
    }

    public static Order create(
            String orderId,
            String customerId,
            String customerName,
            String customerStatus,
            List<OrderLine> lines) {
        BigDecimal total =
                lines.stream().map(OrderLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(
                orderId,
                customerId,
                customerName,
                customerStatus,
                lines,
                total,
                Instant.now(),
                OrderStatus.COMPLETED);
    }
}
