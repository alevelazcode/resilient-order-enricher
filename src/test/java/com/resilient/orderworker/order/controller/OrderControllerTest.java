/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.order.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.resilient.orderworker.order.entity.Order;
import com.resilient.orderworker.order.repository.OrderRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for OrderController REST endpoints.
 *
 * <p>These tests verify the REST API functionality including proper HTTP status codes, response
 * structure, and error handling. They use mocked repository to focus on the web layer.
 *
 * @author Resilient Order Enricher Team
 * @since 1.0.0
 */
@WebFluxTest(OrderController.class)
@DisplayName("Order Controller API Tests")
class OrderControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockBean private OrderRepository orderRepository;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = createSampleOrder();
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Should return order when found")
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Given
        when(orderRepository.findByOrderId("order-123")).thenReturn(Mono.just(sampleOrder));

        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/orders/order-123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.orderId")
                .isEqualTo("order-123")
                .jsonPath("$.customerId")
                .isEqualTo("customer-456")
                .jsonPath("$.customerName")
                .isEqualTo("John Doe")
                .jsonPath("$.customerStatus")
                .isEqualTo("ACTIVE")
                .jsonPath("$.status")
                .isEqualTo("PROCESSED")
                .jsonPath("$.totalAmount")
                .isEqualTo(1299.99)
                .jsonPath("$.products")
                .isArray()
                .jsonPath("$.products[0].productId")
                .isEqualTo("product-001")
                .jsonPath("$.products[0].name")
                .isEqualTo("Gaming Laptop");
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Should return 404 when order not found")
    void getOrderById_WhenOrderNotFound_ShouldReturn404() {
        // Given
        when(orderRepository.findByOrderId("order-999")).thenReturn(Mono.empty());

        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/orders/order-999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    @DisplayName("GET /api/v1/orders/customer/{customerId} - Should return customer orders")
    void getOrdersByCustomerId_WhenOrdersExist_ShouldReturnOrders() {
        // Given
        when(orderRepository.findByCustomerId("customer-456")).thenReturn(Flux.just(sampleOrder));

        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/orders/customer/customer-456")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$")
                .isArray()
                .jsonPath("$[0].orderId")
                .isEqualTo("order-123")
                .jsonPath("$[0].customerId")
                .isEqualTo("customer-456");
    }

    @Test
    @DisplayName(
            "GET /api/v1/orders/customer/{customerId} - Should return 404 when no orders found")
    void getOrdersByCustomerId_WhenNoOrdersFound_ShouldReturn404() {
        // Given
        when(orderRepository.findByCustomerId("customer-999")).thenReturn(Flux.empty());

        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/orders/customer/customer-999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    @DisplayName("GET /api/v1/orders - Should return paginated orders")
    void listOrders_WithDefaultParameters_ShouldReturnPaginatedResponse() {
        // Given
        when(orderRepository.findAll()).thenReturn(Flux.just(sampleOrder));
        when(orderRepository.count()).thenReturn(Mono.just(1L));

        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/orders")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content")
                .isArray()
                .jsonPath("$.content[0].orderId")
                .isEqualTo("order-123")
                .jsonPath("$.page")
                .isEqualTo(0)
                .jsonPath("$.size")
                .isEqualTo(20)
                .jsonPath("$.totalElements")
                .isEqualTo(1)
                .jsonPath("$.totalPages")
                .isEqualTo(1)
                .jsonPath("$.first")
                .isEqualTo(true)
                .jsonPath("$.last")
                .isEqualTo(true);
    }

    @Test
    @DisplayName("GET /api/v1/orders - Should handle custom pagination parameters")
    void listOrders_WithCustomParameters_ShouldRespectPagination() {
        // Given
        when(orderRepository.findAll()).thenReturn(Flux.empty());
        when(orderRepository.count()).thenReturn(Mono.just(0L));

        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/orders?page=1&size=10&sort=orderId,asc")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.page")
                .isEqualTo(1)
                .jsonPath("$.size")
                .isEqualTo(10)
                .jsonPath("$.content")
                .isArray();
    }

    @Test
    @DisplayName("GET /api/v1/orders - Should handle status filter")
    void listOrders_WithStatusFilter_ShouldFilterByStatus() {
        // Given
        when(orderRepository.findByStatus(anyString())).thenReturn(Flux.just(sampleOrder));
        when(orderRepository.countByStatus(anyString())).thenReturn(Mono.just(1L));

        // When & Then
        webTestClient
                .get()
                .uri("/api/v1/orders?status=PROCESSED")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content[0].status")
                .isEqualTo("PROCESSED");
    }

    /**
     * Creates a sample order for testing purposes.
     *
     * @return Sample order with enriched data
     */
    private Order createSampleOrder() {
        Order order = new Order();
        order.setOrderId("order-123");
        order.setCustomerId("customer-456");
        order.setCustomerName("John Doe");
        order.setCustomerStatus("ACTIVE");
        order.setStatus(Order.OrderStatus.PROCESSED);
        order.setTotalAmount(1299.99);
        order.setProcessedAt(LocalDateTime.now(ZoneId.systemDefault()));

        Order.OrderProduct product = new Order.OrderProduct();
        product.setProductId("product-001");
        product.setName("Gaming Laptop");
        product.setDescription("High-performance gaming laptop with RTX graphics");
        product.setPrice(1299.99);
        product.setQuantity(1);
        product.setSubtotal(1299.99);

        order.setProducts(List.of(product));

        return order;
    }
}
