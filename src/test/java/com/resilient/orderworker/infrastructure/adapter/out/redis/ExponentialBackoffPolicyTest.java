/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class ExponentialBackoffPolicyTest {

    private final ExponentialBackoffPolicy policy = new ExponentialBackoffPolicy();

    @Test
    void doublesDelayUntilCapAtFiveMinutes() {
        assertThat(policy.nextDelay(1)).isEqualTo(Duration.ofSeconds(1));
        assertThat(policy.nextDelay(2)).isEqualTo(Duration.ofSeconds(2));
        assertThat(policy.nextDelay(3)).isEqualTo(Duration.ofSeconds(4));
        assertThat(policy.nextDelay(4)).isEqualTo(Duration.ofSeconds(8));
        assertThat(policy.nextDelay(5)).isEqualTo(Duration.ofSeconds(16));
        assertThat(policy.nextDelay(20)).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void maxAttemptsIsFive() {
        assertThat(policy.maxAttempts()).isEqualTo(5);
    }

    @Test
    void rejectsZeroOrNegativeAttempts() {
        assertThatThrownBy(() -> policy.nextDelay(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy.nextDelay(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
