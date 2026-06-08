/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import com.resilient.orderworker.application.command.ProcessOrderCommand;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FailedMessageStore {

    Mono<Void> store(ProcessOrderCommand command, Throwable error);

    Flux<FailedMessage> readyForRetry();

    Mono<Void> remove(String orderId);

    Mono<Long> pendingCount();

    Mono<Long> deadLetterCount();

    record FailedMessage(
            ProcessOrderCommand command, int attemptCount, long nextRetryEpochMillis) {}
}
