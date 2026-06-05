/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void create_computesTotalAndDefaults() {
        OrderLine line1 = new OrderLine("p1", "P1", "d", new BigDecimal("10.00"), 2);
        OrderLine line2 = new OrderLine("p2", "P2", "d", new BigDecimal("5.50"), 4);

        Order order = Order.create("o1", "c1", "Alice", "ACTIVE", List.of(line1, line2));

        assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("42.00"));
        assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.processedAt()).isNotNull();
        assertThat(order.lines()).hasSize(2);
    }

    @Test
    void rejectsEmptyLines() {
        assertThatThrownBy(() -> Order.create("o1", "c1", "n", "ACTIVE", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderLine_validates() {
        assertThatThrownBy(() -> new OrderLine("p1", "n", "d", new BigDecimal("-1"), 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrderLine("p1", "n", "d", BigDecimal.ONE, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderLine_subtotalIsExact() {
        OrderLine line = new OrderLine("p", "n", "d", new BigDecimal("0.10"), 3);
        assertThat(line.subtotal()).isEqualByComparingTo(new BigDecimal("0.30"));
    }
}
