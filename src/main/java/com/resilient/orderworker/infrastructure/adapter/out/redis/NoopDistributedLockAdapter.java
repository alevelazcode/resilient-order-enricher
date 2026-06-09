/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.application.port.out.LockKey;

import reactor.core.publisher.Mono;

@Configuration
@ConditionalOnProperty(name = "redisson.enabled", havingValue = "false")
public class NoopDistributedLockAdapter {

    @Bean
    public DistributedLock noopDistributedLock() {
        return new NoopLock();
    }

    private static final class NoopLock implements DistributedLock {
        @Override
        public <T> Mono<T> executeWithLock(LockKey key, Supplier<Mono<T>> task) {
            return task.get();
        }

        @Override
        public <T> Mono<T> executeWithLock(
                LockKey key, Duration waitTime, Duration leaseTime, Supplier<Mono<T>> task) {
            return task.get();
        }
    }
}
