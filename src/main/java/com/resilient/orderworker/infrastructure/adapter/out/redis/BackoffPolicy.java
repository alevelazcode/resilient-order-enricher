/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;

/**
 * Strategy for computing retry timing of failed messages. Kept as a strategy so the Redis-backed
 * store can be tested with deterministic delays and so the policy can be swapped without touching
 * persistence code.
 */
public interface BackoffPolicy {

    int maxAttempts();

    Duration nextDelay(int attempt);
}
