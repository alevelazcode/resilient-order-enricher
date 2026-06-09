/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderLine;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockBean private QueryOrdersUseCase queryOrders;

    @Test
    void getById_returnsOrder() {
        when(queryOrders.findByOrderId("o1")).thenReturn(Mono.just(sample()));

        webTestClient
                .get()
                .uri("/api/v1/orders/o1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.orderId")
                .isEqualTo("o1")
                .jsonPath("$.totalAmount")
                .isEqualTo(10);
    }

    @Test
    void getById_returns404WhenMissing() {
        when(queryOrders.findByOrderId("missing")).thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("/api/v1/orders/missing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    void getById_returns500OnUpstreamError() {
        when(queryOrders.findByOrderId("boom"))
                .thenReturn(Mono.error(new RuntimeException("db down")));

        webTestClient
                .get()
                .uri("/api/v1/orders/boom")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void list_returnsPage() {
        when(queryOrders.findAll(any()))
                .thenReturn(Mono.just(new QueryOrdersUseCase.Page<>(List.of(sample()), 0, 20, 1L)));

        webTestClient
                .get()
                .uri("/api/v1/orders")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[0].orderId")
                .isEqualTo("o1")
                .jsonPath("$.totalElements")
                .isEqualTo(1);
    }

    @Test
    void list_rejectsInvalidPageSize() {
        webTestClient
                .get()
                .uri("/api/v1/orders?size=500")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("INVALID_REQUEST");
    }

    @Test
    void getByCustomer_returnsEmptyPageWhenNoOrders() {
        when(queryOrders.findAll(any()))
                .thenReturn(Mono.just(new QueryOrdersUseCase.Page<>(List.of(), 0, 20, 0L)));

        webTestClient
                .get()
                .uri("/api/v1/orders/customer/c1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content")
                .isArray()
                .jsonPath("$.content.length()")
                .isEqualTo(0)
                .jsonPath("$.totalElements")
                .isEqualTo(0);
    }

    private Order sample() {
        return Order.fromLines(
                "o1",
                "c1",
                "Alice",
                "ACTIVE",
                List.of(new OrderLine("p1", "Laptop", "d", new BigDecimal("10"), 1)),
                Instant.parse("2026-01-01T00:00:00Z"),
                OrderStatus.COMPLETED);
    }
}
