/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.domain.exception.OrderProcessingException;

import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "redisson.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonDistributedLockAdapter implements DistributedLock {

    private static final Logger LOG = LoggerFactory.getLogger(RedissonDistributedLockAdapter.class);
    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_LEASE = Duration.ofSeconds(30);

    private final RedissonReactiveClient reactive;

    public RedissonDistributedLockAdapter(RedissonClient redisson) {
        this.reactive = redisson.reactive();
    }

    @Override
    public <T> Mono<T> executeWithLock(String key, Supplier<Mono<T>> task) {
        return executeWithLock(key, DEFAULT_WAIT, DEFAULT_LEASE, task);
    }

    @Override
    public <T> Mono<T> executeWithLock(
            String key, Duration waitTime, Duration leaseTime, Supplier<Mono<T>> task) {
        RLockReactive lock = reactive.getLock(key);
        return lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS)
                .flatMap(
                        acquired -> {
                            if (Boolean.FALSE.equals(acquired)) {
                                return Mono.error(
                                        new OrderProcessingException(
                                                "Could not acquire lock: " + key));
                            }
                            return task.get()
                                    .flatMap(result -> safeUnlock(lock, key).thenReturn(result))
                                    .onErrorResume(
                                            err -> safeUnlock(lock, key).then(Mono.error(err)));
                        });
    }

    private static Mono<Void> safeUnlock(RLockReactive lock, String key) {
        return lock.unlock()
                .onErrorResume(
                        err -> {
                            LOG.warn("Failed to unlock {}: {}", key, err.toString());
                            return Mono.empty();
                        });
    }
}
