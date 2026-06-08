/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpringDataOrderRepository extends ReactiveMongoRepository<OrderDocument, String> {

    Mono<OrderDocument> findByOrderId(String orderId);

    Mono<Boolean> existsByOrderId(String orderId);

    Flux<OrderDocument> findByCustomerId(String customerId, Pageable pageable);

    Flux<OrderDocument> findByStatus(String status, Pageable pageable);

    Flux<OrderDocument> findByStatusAndCustomerId(
            String status, String customerId, Pageable pageable);

    Flux<OrderDocument> findAllBy(Pageable pageable);

    Mono<Long> countByStatus(String status);

    Mono<Long> countByCustomerId(String customerId);

    Mono<Long> countByStatusAndCustomerId(String status, String customerId);
}
