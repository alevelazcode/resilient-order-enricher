/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.order;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderLine(
        String productId, String name, String description, BigDecimal unitPrice, int quantity) {

    public OrderLine {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
