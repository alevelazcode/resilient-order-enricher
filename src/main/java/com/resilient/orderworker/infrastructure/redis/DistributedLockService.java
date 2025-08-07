/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import java.util.function.Supplier;

import reactor.core.publisher.Mono;

/**
 * Service for distributed locking. Prevents concurrent processing of the same order
 * across multiple instances.
 */
public interface DistributedLockService {

    /**
     * Executes a task with a distributed lock on the given order ID.
     *
     * @param orderId the order ID to lock
     * @param task the task to execute
     * @return Mono containing the result of the task
     */
    <T> Mono<T> executeWithLock(String orderId, Supplier<Mono<T>> task);

    /**
     * Executes a task with a distributed lock on the given order ID with custom timeouts.
     *
     * @param orderId the order ID to lock
     * @param task the task to execute
     * @param waitTime maximum time to wait for the lock
     * @param leaseTime time after which the lock will be automatically released
     * @return Mono containing the result of the task
     */
    <T> Mono<T> executeWithLock(String orderId, Supplier<Mono<T>> task, long waitTime, long leaseTime);

    /**
     * Checks if an order is currently locked.
     *
     * @param orderId the order ID to check
     * @return Mono containing true if the order is locked
     */
    Mono<Boolean> isOrderLocked(String orderId);
}
