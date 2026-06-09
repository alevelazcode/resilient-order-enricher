/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LockKeyTest {

    @Test
    void forOrder_buildsNamespacedKey() {
        assertThat(LockKey.forOrder("o1").asString()).isEqualTo("order-lock:o1");
    }

    @Test
    void rejectsBlankParts() {
        assertThatThrownBy(() -> new LockKey("", "x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LockKey("ns", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullParts() {
        assertThatThrownBy(() -> new LockKey(null, "x")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LockKey("ns", null)).isInstanceOf(NullPointerException.class);
    }
}
