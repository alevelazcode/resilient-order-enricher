/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class NoopFailedMessageAdapter {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public FailedMessageStore noopFailedMessageStore() {
        return new FailedMessageStore() {
            @Override
            public Mono<Void> store(ProcessOrderCommand command, Throwable error) {
                return Mono.empty();
            }

            @Override
            public Flux<FailedMessage> readyForRetry() {
                return Flux.empty();
            }

            @Override
            public Mono<Void> remove(String orderId) {
                return Mono.empty();
            }
        };
    }
}
