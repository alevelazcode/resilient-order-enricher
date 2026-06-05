/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import com.resilient.orderworker.domain.order.Order;

import reactor.core.publisher.Mono;

/**
 * Write-side persistence port. Kept narrow so {@link
 * com.resilient.orderworker.application.service.OrderProcessor} only depends on the operations it
 * actually uses (Interface Segregation).
 */
public interface OrderRepository {

    Mono<Order> save(Order order);

    Mono<Order> findByOrderId(String orderId);

    Mono<Boolean> existsByOrderId(String orderId);
}
