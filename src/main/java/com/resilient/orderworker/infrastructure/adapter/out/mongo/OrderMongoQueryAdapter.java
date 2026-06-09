/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.application.port.out.OrderQueryRepository;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Read-side Mongo adapter: pageable lookups + counts, no mutations. */
@Component
public class OrderMongoQueryAdapter implements OrderQueryRepository {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "processedAt");

    private final SpringDataOrderRepository repository;

    public OrderMongoQueryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Order> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId).map(OrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<Order> findAll(int page, int size) {
        return repository.findAllBy(pageable(page, size)).map(OrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<Order> findByStatus(OrderStatus status, int page, int size) {
        return repository
                .findByStatus(status.name(), pageable(page, size))
                .map(OrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<Order> findByCustomerId(String customerId, int page, int size) {
        return repository
                .findByCustomerId(customerId, pageable(page, size))
                .map(OrderDocumentMapper::toDomain);
    }

    @Override
    public Flux<Order> findByStatusAndCustomerId(
            OrderStatus status, String customerId, int page, int size) {
        return repository
                .findByStatusAndCustomerId(status.name(), customerId, pageable(page, size))
                .map(OrderDocumentMapper::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }

    @Override
    public Mono<Long> countByStatus(OrderStatus status) {
        return repository.countByStatus(status.name());
    }

    @Override
    public Mono<Long> countByCustomerId(String customerId) {
        return repository.countByCustomerId(customerId);
    }

    @Override
    public Mono<Long> countByStatusAndCustomerId(OrderStatus status, String customerId) {
        return repository.countByStatusAndCustomerId(status.name(), customerId);
    }

    private static Pageable pageable(int page, int size) {
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}
