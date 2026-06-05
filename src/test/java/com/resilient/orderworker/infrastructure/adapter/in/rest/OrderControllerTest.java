/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockBean private QueryOrdersUseCase queryOrders;

    @Test
    void getById_returnsOrder() {
        Order order = sample();
        when(queryOrders.findByOrderId("o1")).thenReturn(Mono.just(order));

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
    void getByCustomer_returns404WhenEmpty() {
        when(queryOrders.findByCustomerId("c1")).thenReturn(Flux.empty());

        webTestClient
                .get()
                .uri("/api/v1/orders/customer/c1")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    private Order sample() {
        return Order.create(
                "o1",
                "c1",
                "Alice",
                "ACTIVE",
                List.of(new OrderLine("p1", "Laptop", "d", new BigDecimal("10"), 1)));
    }
}
