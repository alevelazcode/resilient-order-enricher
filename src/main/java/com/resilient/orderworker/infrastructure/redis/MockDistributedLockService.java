/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Mock implementation of DistributedLockService for when Redis is not available.
 * This service simply executes the task without any locking mechanism.
 */
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false")
public class MockDistributedLockService implements DistributedLockService {

    private static final Logger logger = LoggerFactory.getLogger(MockDistributedLockService.class);

    @Override
    public <T> Mono<T> executeWithLock(String orderId, Supplier<Mono<T>> task) {
        logger.debug("Mock lock service: executing task for order: {}", orderId);
        return task.get();
    }

    @Override
    public <T> Mono<T> executeWithLock(String orderId, Supplier<Mono<T>> task, long waitTime, long leaseTime) {
        logger.debug("Mock lock service: executing task for order: {} (waitTime: {}, leaseTime: {})", 
                    orderId, waitTime, leaseTime);
        return task.get();
    }

    @Override
    public Mono<Boolean> isOrderLocked(String orderId) {
        logger.debug("Mock lock service: checking if order is locked: {}", orderId);
        return Mono.just(false);
    }
}
