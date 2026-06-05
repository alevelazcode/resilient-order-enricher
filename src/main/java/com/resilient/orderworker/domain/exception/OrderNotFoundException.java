/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.exception;

public class OrderNotFoundException extends DomainException {

    public OrderNotFoundException(String orderId) {
        super("Order with ID '" + orderId + "' was not found");
    }
}
