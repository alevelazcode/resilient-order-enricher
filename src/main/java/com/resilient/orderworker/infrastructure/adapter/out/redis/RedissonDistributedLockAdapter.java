/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.domain.exception.OrderProcessingException;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@ConditionalOnBean(RedissonClient.class)
public class RedissonDistributedLockAdapter implements DistributedLock {

    private static final Logger LOG = LoggerFactory.getLogger(RedissonDistributedLockAdapter.class);
    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_LEASE = Duration.ofSeconds(30);

    private final RedissonClient redisson;

    public RedissonDistributedLockAdapter(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public <T> Mono<T> executeWithLock(String key, Supplier<Mono<T>> task) {
        return executeWithLock(key, DEFAULT_WAIT, DEFAULT_LEASE, task);
    }

    @Override
    public <T> Mono<T> executeWithLock(
            String key, Duration waitTime, Duration leaseTime, Supplier<Mono<T>> task) {
        return Mono.fromCallable(() -> acquire(key, waitTime, leaseTime))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(
                        lock ->
                                task.get()
                                        .doFinally(
                                                signal -> {
                                                    try {
                                                        if (lock.isHeldByCurrentThread()) {
                                                            lock.unlock();
                                                        }
                                                    } catch (Exception e) {
                                                        LOG.warn(
                                                                "Failed to unlock {}: {}",
                                                                key,
                                                                e.toString());
                                                    }
                                                }));
    }

    private RLock acquire(String key, Duration waitTime, Duration leaseTime) {
        RLock lock = redisson.getLock(key);
        try {
            boolean acquired =
                    lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new OrderProcessingException("Could not acquire lock: " + key);
            }
            return lock;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderProcessingException("Interrupted acquiring lock: " + key, e);
        }
    }
}
