/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.application.port.out.OrderQueryRepository;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderLine;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock private OrderQueryRepository repository;

    @Test
    void findAll_pagesAcrossAllFilterBranches() {
        OrderQueryService service = new OrderQueryService(repository);
        Order order = sampleOrder();

        when(repository.findAll(0, 20)).thenReturn(Flux.just(order));
        when(repository.count()).thenReturn(Mono.just(1L));

        StepVerifier.create(service.findAll(new QueryOrdersUseCase.PageQuery(0, 20, null, null)))
                .assertNext(
                        page -> {
                            assertThat(page.content()).hasSize(1);
                            assertThat(page.totalElements()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.first()).isTrue();
                            assertThat(page.last()).isTrue();
                        })
                .verifyComplete();
    }

    @Test
    void findAll_filtersByStatus() {
        OrderQueryService service = new OrderQueryService(repository);

        when(repository.findByStatus(OrderStatus.COMPLETED, 0, 5))
                .thenReturn(Flux.just(sampleOrder()));
        when(repository.countByStatus(OrderStatus.COMPLETED)).thenReturn(Mono.just(1L));

        StepVerifier.create(
                        service.findAll(
                                new QueryOrdersUseCase.PageQuery(
                                        0, 5, OrderStatus.COMPLETED, null)))
                .assertNext(page -> assertThat(page.content()).hasSize(1))
                .verifyComplete();
    }

    private Order sampleOrder() {
        return Order.create(
                "o1",
                "c1",
                "Alice",
                "ACTIVE",
                List.of(new OrderLine("p1", "P", "d", new BigDecimal("10"), 1)));
    }
}
