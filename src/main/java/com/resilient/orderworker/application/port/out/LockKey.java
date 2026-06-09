/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import java.util.Objects;

/** Stable, type-safe key for the distributed lock so callers stop concatenating strings. */
public record LockKey(String namespace, String value) {

    public LockKey {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(value, "value");
        if (namespace.isBlank() || value.isBlank()) {
            throw new IllegalArgumentException("LockKey parts must not be blank");
        }
    }

    public static LockKey forOrder(String orderId) {
        return new LockKey("order-lock", orderId);
    }

    public String asString() {
        return namespace + ":" + value;
    }
}
