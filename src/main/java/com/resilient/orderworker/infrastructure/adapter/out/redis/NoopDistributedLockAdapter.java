/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;
import java.util.function.Supplier;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resilient.orderworker.application.port.out.DistributedLock;

import reactor.core.publisher.Mono;

@Configuration
public class NoopDistributedLockAdapter {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public DistributedLock noopDistributedLock() {
        return new DistributedLock() {
            @Override
            public <T> Mono<T> executeWithLock(String key, Supplier<Mono<T>> task) {
                return task.get();
            }

            @Override
            public <T> Mono<T> executeWithLock(
                    String key, Duration waitTime, Duration leaseTime, Supplier<Mono<T>> task) {
                return task.get();
            }
        };
    }
}
