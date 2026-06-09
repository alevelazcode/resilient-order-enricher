/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FailedMessageHealthIndicatorTest {

    @Mock private FailedMessageStore store;

    @Test
    void up_whenDeadLetterIsEmpty() {
        when(store.pendingCount()).thenReturn(Mono.just(3L));
        when(store.deadLetterCount()).thenReturn(Mono.just(0L));

        StepVerifier.create(new FailedMessageHealthIndicator(store).health())
                .assertNext(
                        health -> {
                            assertThat(health.getStatus()).isEqualTo(Status.UP);
                            assertThat(health.getDetails())
                                    .containsEntry("pending", 3L)
                                    .containsEntry("deadLetter", 0L);
                        })
                .verifyComplete();
    }

    @Test
    void outOfService_whenDeadLetterNonEmpty() {
        when(store.pendingCount()).thenReturn(Mono.just(1L));
        when(store.deadLetterCount()).thenReturn(Mono.just(7L));

        StepVerifier.create(new FailedMessageHealthIndicator(store).health())
                .assertNext(
                        health -> {
                            assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
                            assertThat(health.getDetails()).containsEntry("deadLetter", 7L);
                        })
                .verifyComplete();
    }

    @Test
    void down_whenBackendErrors() {
        when(store.pendingCount()).thenReturn(Mono.error(new RuntimeException("redis off")));
        when(store.deadLetterCount()).thenReturn(Mono.just(0L));

        StepVerifier.create(new FailedMessageHealthIndicator(store).health())
                .assertNext(health -> assertThat(health.getStatus()).isEqualTo(Status.DOWN))
                .verifyComplete();
    }

    @SuppressWarnings("unused")
    private Health unused() {
        return Health.up().build();
    }
}
