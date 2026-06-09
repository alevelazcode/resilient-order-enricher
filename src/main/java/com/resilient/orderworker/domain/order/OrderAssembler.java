/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.order;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.product.Product;

/**
 * Pure domain builder that turns a validated command + enriched data into an {@link Order}. Keeps
 * the timestamp behind a {@link Clock} so tests can pin time without touching {@code Instant.now()}
 * statics.
 */
public final class OrderAssembler {

    private final Clock clock;

    public OrderAssembler(Clock clock) {
        this.clock = clock;
    }

    public Order assemble(
            ProcessOrderCommand command, Customer customer, Map<String, Product> productById) {
        List<OrderLine> lines =
                command.lines().stream()
                        .map(
                                line ->
                                        toOrderLine(
                                                productById.get(line.productId()), line.quantity()))
                        .toList();

        return Order.fromLines(
                command.orderId(),
                customer.customerId(),
                customer.name(),
                customer.status(),
                lines,
                Instant.now(clock),
                OrderStatus.COMPLETED);
    }

    private OrderLine toOrderLine(Product product, int quantity) {
        return new OrderLine(
                product.productId(),
                product.name(),
                product.description(),
                product.price(),
                quantity);
    }
}
