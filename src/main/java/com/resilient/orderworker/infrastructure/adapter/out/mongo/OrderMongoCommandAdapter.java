/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import org.springframework.stereotype.Component;

import com.resilient.orderworker.application.port.out.OrderRepository;
import com.resilient.orderworker.domain.order.Order;

import reactor.core.publisher.Mono;

/** Write-side Mongo adapter: persistence + idempotency check, nothing else. */
@Component
public class OrderMongoCommandAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    public OrderMongoCommandAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Order> save(Order order) {
        return repository
                .save(OrderDocumentMapper.toDocument(order))
                .map(OrderDocumentMapper::toDomain);
    }

    @Override
    public Mono<Order> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId).map(OrderDocumentMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByOrderId(String orderId) {
        return repository.existsByOrderId(orderId);
    }
}
