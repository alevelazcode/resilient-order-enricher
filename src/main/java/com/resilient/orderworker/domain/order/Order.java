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

    /**
     * Build an {@link Order} from already-validated lines. The total is computed from the lines so
     * callers cannot pass an inconsistent {@code totalAmount}.
     */
    public static Order fromLines(
            String orderId,
            String customerId,
            String customerName,
            String customerStatus,
            List<OrderLine> lines,
            Instant processedAt,
            OrderStatus status) {
        BigDecimal total =
                lines.stream().map(OrderLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(
                orderId,
                customerId,
                customerName,
                customerStatus,
                lines,
                total,
                processedAt,
                status);
    }
}
