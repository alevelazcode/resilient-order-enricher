/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.customer;

public record Customer(String customerId, String name, String status) {

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
