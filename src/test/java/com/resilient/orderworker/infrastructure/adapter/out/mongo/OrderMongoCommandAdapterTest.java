/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderLine;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderMongoCommandAdapterTest {

    @Mock private SpringDataOrderRepository repository;

    @Test
    void save_mapsDomainToDocumentAndBack() {
        Order order = sample();
        when(repository.save(any(OrderDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(new OrderMongoCommandAdapter(repository).save(order))
                .assertNext(
                        persisted -> {
                            org.assertj.core.api.Assertions.assertThat(persisted.orderId())
                                    .isEqualTo(order.orderId());
                            org.assertj.core.api.Assertions.assertThat(persisted.totalAmount())
                                    .isEqualByComparingTo(order.totalAmount());
                        })
                .verifyComplete();
    }

    @Test
    void existsByOrderId_delegatesToRepository() {
        when(repository.existsByOrderId("o1")).thenReturn(Mono.just(true));
        StepVerifier.create(new OrderMongoCommandAdapter(repository).existsByOrderId("o1"))
                .expectNext(true)
                .verifyComplete();
    }

    private Order sample() {
        return Order.fromLines(
                "o1",
                "c1",
                "Alice",
                "ACTIVE",
                List.of(new OrderLine("p1", "Laptop", "d", new BigDecimal("100.00"), 2)),
                Instant.parse("2026-01-01T00:00:00Z"),
                OrderStatus.COMPLETED);
    }
}
