/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderLine;
import com.resilient.orderworker.domain.order.OrderStatus;

class OrderDocumentMapperTest {

    private static final Instant PROCESSED_AT = Instant.parse("2026-06-08T12:00:00Z");

    @Test
    void roundTrip_preservesAllFields() {
        Order original =
                Order.fromLines(
                        "o1",
                        "c1",
                        "Alice",
                        "ACTIVE",
                        List.of(
                                new OrderLine(
                                        "p1", "Laptop", "Gaming", new BigDecimal("999.99"), 2)),
                        PROCESSED_AT,
                        OrderStatus.COMPLETED);

        OrderDocument document = OrderDocumentMapper.toDocument(original);
        Order roundTripped = OrderDocumentMapper.toDomain(document);

        assertThat(roundTripped.orderId()).isEqualTo("o1");
        assertThat(roundTripped.customerId()).isEqualTo("c1");
        assertThat(roundTripped.customerName()).isEqualTo("Alice");
        assertThat(roundTripped.customerStatus()).isEqualTo("ACTIVE");
        assertThat(roundTripped.processedAt()).isEqualTo(PROCESSED_AT);
        assertThat(roundTripped.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(roundTripped.totalAmount()).isEqualByComparingTo(new BigDecimal("1999.98"));
        assertThat(roundTripped.lines()).hasSize(1);
        assertThat(roundTripped.lines().get(0).productId()).isEqualTo("p1");
        assertThat(roundTripped.lines().get(0).unitPrice())
                .isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(roundTripped.lines().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void documentStoresEnumAsName() {
        Order order =
                Order.fromLines(
                        "o1",
                        "c1",
                        "Alice",
                        "ACTIVE",
                        List.of(new OrderLine("p1", "n", "d", BigDecimal.ONE, 1)),
                        PROCESSED_AT,
                        OrderStatus.FAILED);
        assertThat(OrderDocumentMapper.toDocument(order).getStatus()).isEqualTo("FAILED");
    }
}
