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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Redis implementation of DistributedLockService using Redisson. */
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisDistributedLockService implements DistributedLockService {

    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLockService.class);
    private static final String LOCK_PREFIX = "order-lock:";
    private static final long DEFAULT_WAIT_TIME = 10L; // seconds
    private static final long DEFAULT_LEASE_TIME = 30L; // seconds

    private final RedissonClient redissonClient;

    public RedisDistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> Mono<T> executeWithLock(String orderId, Supplier<Mono<T>> task) {
        return executeWithLock(orderId, task, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    @Override
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
                                                                "Error releasing lock for order:"
                                                                        + " {}",
                                                                orderId,
                                                                e);
                                                    }
                                                }));
    }

    @Override
    public Mono<Boolean> isOrderLocked(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        return Mono.just(lock.isLocked());
    }
}
