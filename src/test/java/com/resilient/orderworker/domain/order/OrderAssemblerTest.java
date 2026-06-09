/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.product.Product;

class OrderAssemblerTest {

    private static final Instant FIXED = Instant.parse("2026-06-08T12:00:00Z");

    private final OrderAssembler assembler = new OrderAssembler(Clock.fixed(FIXED, ZoneOffset.UTC));

    @Test
    void assemble_buildsOrderFromCommandAndEnrichedData() {
        ProcessOrderCommand cmd =
                new ProcessOrderCommand(
                        "o1",
                        "c1",
                        List.of(
                                new ProcessOrderCommand.Line("p1", 2),
                                new ProcessOrderCommand.Line("p2", 1)));
        Customer customer = new Customer("c1", "Alice", "ACTIVE");
        Map<String, Product> productById =
                Map.of(
                        "p1", new Product("p1", "Laptop", "d", new BigDecimal("100.00")),
                        "p2", new Product("p2", "Mouse", "d", new BigDecimal("50.00")));

        Order order = assembler.assemble(cmd, customer, productById);

        assertThat(order.orderId()).isEqualTo("o1");
        assertThat(order.customerId()).isEqualTo("c1");
        assertThat(order.customerName()).isEqualTo("Alice");
        assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(order.processedAt()).isEqualTo(FIXED);
        assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.lines()).hasSize(2);
    }
}
