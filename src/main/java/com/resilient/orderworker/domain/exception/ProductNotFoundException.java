/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.exception;

public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}
