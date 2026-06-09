/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.order;

import java.util.Map;

import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.exception.OrderProcessingException;
import com.resilient.orderworker.domain.product.Product;

/**
 * Pure domain validator for the order pipeline. No Spring, no I/O, no logging — the rules live in
 * one place so the application service can stay focused on orchestration.
 */
public final class OrderValidator {

    public void requireActiveCustomer(Customer customer) {
        if (!customer.isActive()) {
            throw new OrderProcessingException("Customer is not active: " + customer.customerId());
        }
    }

    public void requireAllProductsResolved(
            Iterable<String> requiredIds, Map<String, Product> productById) {
        for (String id : requiredIds) {
            Product product = productById.get(id);
            if (product == null) {
                throw new OrderProcessingException("Product not found: " + id);
            }
            if (!product.isValid()) {
                throw new OrderProcessingException("Invalid product: " + id);
            }
        }
    }
}
