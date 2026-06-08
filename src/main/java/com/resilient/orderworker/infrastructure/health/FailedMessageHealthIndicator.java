/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Mono;

/**
 * Exposes the size of the failed-message and dead-letter queues at {@code /actuator/health}.
 * Returns {@code OUT_OF_SERVICE} when the dead-letter queue is non-empty (manual intervention
 * required) and {@code UP} otherwise.
 */
@Component("failedMessages")
public class FailedMessageHealthIndicator implements ReactiveHealthIndicator {

    private final FailedMessageStore failedMessageStore;

    public FailedMessageHealthIndicator(FailedMessageStore failedMessageStore) {
        this.failedMessageStore = failedMessageStore;
    }

    @Override
    public Mono<Health> health() {
        return Mono.zip(failedMessageStore.pendingCount(), failedMessageStore.deadLetterCount())
                .map(
                        counts -> {
                            long pending = counts.getT1();
                            long deadLetter = counts.getT2();
                            Status status = deadLetter > 0 ? Status.OUT_OF_SERVICE : Status.UP;
                            return Health.status(status)
                                    .withDetail("pending", pending)
                                    .withDetail("deadLetter", deadLetter)
                                    .build();
                        })
                .onErrorResume(
                        err ->
                                Mono.just(
                                        Health.down(err)
                                                .withDetail("reason", err.toString())
                                                .build()));
    }
}
