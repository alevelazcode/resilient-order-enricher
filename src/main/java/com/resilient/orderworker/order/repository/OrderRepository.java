/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.order.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.resilient.orderworker.order.entity.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for Order entities.
 *
 * <p>This repository provides reactive data access methods for Order entities with support for
 * pagination, filtering, and custom queries. All methods return reactive types (Mono/Flux) for
 * non-blocking database operations.
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {

    /**
     * Find an order by its orderId.
     *
     * @param orderId the order ID
     * @return Mono containing the order if found
     */
    Mono<Order> findByOrderId(String orderId);

    /**
     * Check if an order exists by orderId.
     *
     * @param orderId the order ID
     * @return Mono containing true if exists
     */
    Mono<Boolean> existsByOrderId(String orderId);

    /**
     * Find all orders for a specific customer.
     *
     * @param customerId the customer ID
     * @return Flux of orders for the customer
     */
    Flux<Order> findByCustomerId(String customerId);

    /**
     * Count orders for a specific customer.
     *
     * @param customerId the customer ID
     * @return Mono containing the count
     */
    Mono<Long> countByCustomerId(String customerId);

    /**
     * Find orders by processing status.
     *
     * @param status the processing status
     * @return Flux of orders with the specified status
     */
    Flux<Order> findByStatus(String status);

    /**
     * Count orders by processing status.
     *
     * @param status the processing status
     * @return Mono containing the count
     */
    Mono<Long> countByStatus(String status);

    /**
     * Find orders by status and customer ID.
     *
     * @param status the processing status
     * @param customerId the customer ID
     * @return Flux of orders matching both criteria
     */
    Flux<Order> findByStatusAndCustomerId(String status, String customerId);

    /**
     * Count orders by status and customer ID.
     *
     * @param status the processing status
     * @param customerId the customer ID
     * @return Mono containing the count
     */
    Mono<Long> countByStatusAndCustomerId(String status, String customerId);
}
