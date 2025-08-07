/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service for distributed locking using Redis. Prevents concurrent processing of the same order
 * across multiple instances.
 */
@Service
public class DistributedLockService {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String LOCK_PREFIX = "order-lock:";
    private static final long DEFAULT_WAIT_TIME = 10L; // seconds
    private static final long DEFAULT_LEASE_TIME = 30L; // seconds

    private final RedissonClient redissonClient;

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Executes a task with a distributed lock on the given order ID.
     *
     * @param orderId the order ID to lock
     * @param task the task to execute
     * @return Mono containing the result of the task
     */
    public <T> Mono<T> executeWithLock(String orderId, Supplier<Mono<T>> task) {
        return executeWithLock(orderId, task, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    /**
     * Executes a task with a distributed lock on the given order ID with custom timeouts.
     *
     * @param orderId the order ID to lock
     * @param task the task to execute
     * @param waitTime maximum time to wait for the lock
     * @param leaseTime time after which the lock will be automatically released
     * @return Mono containing the result of the task
     */
    public <T> Mono<T> executeWithLock(
            String orderId, Supplier<Mono<T>> task, long waitTime, long leaseTime) {
        String lockKey = LOCK_PREFIX + orderId;

        return Mono.fromCallable(
                        () -> {
                            RLock lock = redissonClient.getLock(lockKey);
                            logger.debug("Attempting to acquire lock for order: {}", orderId);

                            try {
                                boolean acquired =
                                        lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
                                if (!acquired) {
                                    logger.warn(
                                            "Failed to acquire lock for order: {} within {}"
                                                    + " seconds",
                                            orderId,
                                            waitTime);
                                    throw new RuntimeException(
                                            "Could not acquire lock for order: " + orderId);
                                }

                                logger.debug("Successfully acquired lock for order: {}", orderId);
                                return lock;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(
                                        "Interrupted while acquiring lock for order: " + orderId,
                                        e);
                            }
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(
                        lock ->
                                task.get()
                                        .doFinally(
                                                signalType -> {
                                                    try {
                                                        if (lock.isHeldByCurrentThread()) {
                                                            lock.unlock();
                                                            logger.debug(
                                                                    "Released lock for order: {}",
                                                                    orderId);
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error(
                                                                "Error releasing lock for order {}:"
                                                                        + " {}",
                                                                orderId,
                                                                e.getMessage());
                                                    }
                                                }));
    }

    /**
     * Checks if an order is currently locked.
     *
     * @param orderId the order ID
     * @return Mono containing true if locked
     */
    public Mono<Boolean> isOrderLocked(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;

        return Mono.fromCallable(
                        () -> {
                            RLock lock = redissonClient.getLock(lockKey);
                            return lock.isLocked();
                        })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
