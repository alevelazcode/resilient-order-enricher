/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository {

    Mono<Order> save(Order order);

    Mono<Order> findByOrderId(String orderId);

    Mono<Boolean> existsByOrderId(String orderId);

    Flux<Order> findByCustomerId(String customerId);

    Flux<Order> findAll(int page, int size);

    Flux<Order> findByStatus(OrderStatus status, int page, int size);

    Flux<Order> findByCustomerId(String customerId, int page, int size);

    Flux<Order> findByStatusAndCustomerId(
            OrderStatus status, String customerId, int page, int size);

    Mono<Long> count();

    Mono<Long> countByStatus(OrderStatus status);

    Mono<Long> countByCustomerId(String customerId);

    Mono<Long> countByStatusAndCustomerId(OrderStatus status, String customerId);
}
