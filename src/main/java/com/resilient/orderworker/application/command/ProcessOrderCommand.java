/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.command;

import java.util.List;
import java.util.Objects;

public record ProcessOrderCommand(String orderId, String customerId, List<Line> lines) {

    public ProcessOrderCommand {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(lines, "lines");
        if (orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        if (customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty");
        }
        lines = List.copyOf(lines);
    }

    public record Line(String productId, int quantity) {

        public Line {
            Objects.requireNonNull(productId, "productId");
            if (productId.isBlank()) {
                throw new IllegalArgumentException("productId must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
        }
    }
}
