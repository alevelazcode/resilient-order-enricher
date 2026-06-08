/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** No-op store used when Redisson is disabled (test profile, local-only runs). */
final class NoopFailedMessageStore implements FailedMessageStore {

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

    @Override
    public Mono<Long> pendingCount() {
        return Mono.just(0L);
    }

    @Override
    public Mono<Long> deadLetterCount() {
        return Mono.just(0L);
    }
}
