/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@ConditionalOnProperty(name = "redisson.enabled", havingValue = "false")
public class NoopFailedMessageAdapter {

    @Bean
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
