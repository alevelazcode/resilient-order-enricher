/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.order;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.exception.OrderProcessingException;
import com.resilient.orderworker.domain.product.Product;

class OrderValidatorTest {

    private final OrderValidator validator = new OrderValidator();

    @Test
    void activeCustomerPasses() {
        assertThatCode(() -> validator.requireActiveCustomer(new Customer("c1", "n", "ACTIVE")))
                .doesNotThrowAnyException();
    }

    @Test
    void inactiveCustomerThrows() {
        assertThatThrownBy(
                        () -> validator.requireActiveCustomer(new Customer("c1", "n", "INACTIVE")))
                .isInstanceOf(OrderProcessingException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void missingProductThrows() {
        Map<String, Product> products =
                Map.of("p1", new Product("p1", "n", "d", new BigDecimal("10")));
        assertThatThrownBy(
                        () -> validator.requireAllProductsResolved(List.of("p1", "p2"), products))
                .isInstanceOf(OrderProcessingException.class)
                .hasMessageContaining("Product not found: p2");
    }

    @Test
    void invalidProductThrows() {
        Map<String, Product> products =
                Map.of("p1", new Product("p1", "", "d", new BigDecimal("10")));
        assertThatThrownBy(() -> validator.requireAllProductsResolved(List.of("p1"), products))
                .isInstanceOf(OrderProcessingException.class)
                .hasMessageContaining("Invalid product: p1");
    }
}
