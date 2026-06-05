/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;

import org.springframework.stereotype.Component;

@Component
public class ExponentialBackoffPolicy implements BackoffPolicy {

    private static final int DEFAULT_MAX_ATTEMPTS = 5;
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(1);
    private static final Duration MAX_DELAY = Duration.ofMinutes(5);
    private static final double MULTIPLIER = 2.0;

    @Override
    public int maxAttempts() {
        return DEFAULT_MAX_ATTEMPTS;
    }

    @Override
    public Duration nextDelay(int attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must be >= 1, got " + attempt);
        }
        long millis = (long) (INITIAL_DELAY.toMillis() * Math.pow(MULTIPLIER, attempt - 1));
        return Duration.ofMillis(Math.min(millis, MAX_DELAY.toMillis()));
    }
}
