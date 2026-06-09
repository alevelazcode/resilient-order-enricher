/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderMongoQueryAdapterTest {

    @Mock private SpringDataOrderRepository repository;

    @Test
    void findAll_passesPageableSortedByProcessedAtDesc() {
        when(repository.findAllBy(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Flux.just(sample()));

        StepVerifier.create(new OrderMongoQueryAdapter(repository).findAll(2, 10).collectList())
                .assertNext(list -> assertThat(list).hasSize(1))
                .verifyComplete();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(repository).findAllBy(pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(2);
        assertThat(captured.getPageSize()).isEqualTo(10);
        Sort.Order order = captured.getSort().getOrderFor("processedAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void findByStatus_passesEnumNameToSpringData() {
        when(repository.findByStatus(
                        org.mockito.ArgumentMatchers.eq("COMPLETED"),
                        org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(
                        new OrderMongoQueryAdapter(repository)
                                .findByStatus(OrderStatus.COMPLETED, 0, 20)
                                .collectList())
                .assertNext(list -> assertThat(list).isEmpty())
                .verifyComplete();
    }

    @Test
    void countByStatusAndCustomerId_delegatesWithEnumName() {
        when(repository.countByStatusAndCustomerId("FAILED", "c1")).thenReturn(Mono.just(7L));

        StepVerifier.create(
                        new OrderMongoQueryAdapter(repository)
                                .countByStatusAndCustomerId(OrderStatus.FAILED, "c1"))
                .expectNext(7L)
                .verifyComplete();
    }

    private OrderDocument sample() {
        return new OrderDocument(
                "o1",
                "c1",
                "Alice",
                "ACTIVE",
                List.of(new OrderLineDocument("p1", "Laptop", "d", new BigDecimal("10"), 1)),
                new BigDecimal("10"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "COMPLETED");
    }
}
