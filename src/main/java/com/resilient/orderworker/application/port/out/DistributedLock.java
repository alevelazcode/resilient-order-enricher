/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import java.time.Duration;
import java.util.function.Supplier;

import reactor.core.publisher.Mono;

public interface DistributedLock {

    <T> Mono<T> executeWithLock(LockKey key, Supplier<Mono<T>> task);

    <T> Mono<T> executeWithLock(
            LockKey key, Duration waitTime, Duration leaseTime, Supplier<Mono<T>> task);
}
