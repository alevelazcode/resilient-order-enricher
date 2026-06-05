/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.exception;

public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}
