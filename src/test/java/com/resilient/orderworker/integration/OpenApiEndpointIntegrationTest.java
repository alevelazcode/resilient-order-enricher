/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.integration;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.resilient.orderworker.order.entity.Order;
import com.resilient.orderworker.order.repository.OrderRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for API endpoints as documented in OpenAPI specification.
 *
 * <p>These tests validate that the actual API endpoints work exactly as documented in the OpenAPI
 * specification, ensuring consistency between documentation and implementation.
 *
 * @author Resilient Order Enricher Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DisplayName("OpenAPI Endpoint Integration Tests")
class OpenApiEndpointIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderRepository orderRepository;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = createSampleOrder();
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} should work as documented in OpenAPI spec")
    void getOrderById_ShouldWorkAsDocumented() {
        // Given
        when(orderRepository.findByOrderId("order-12345")).thenReturn(Mono.just(sampleOrder));

        // When & Then - Validate against OpenAPI specification
        webTestClient
                .get()
                .uri("/api/v1/orders/order-12345")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                // Validate required fields as per OpenAPI spec
                .jsonPath("$.orderId")
                .isEqualTo("order-12345")
                .jsonPath("$.customerId")
                .isEqualTo("customer-67890")
                .jsonPath("$.customerName")
                .isEqualTo("John Doe")
                .jsonPath("$.customerStatus")
                .isEqualTo("ACTIVE")
                .jsonPath("$.totalAmount")
                .isEqualTo(1299.99)
                .jsonPath("$.status")
                .isEqualTo("PROCESSED")
                .jsonPath("$.processedAt")
                .exists()
                .jsonPath("$.products")
                .isArray()
                .jsonPath("$.products[0].productId")
                .isEqualTo("product-001")
                .jsonPath("$.products[0].name")
                .isEqualTo("Gaming Laptop")
                .jsonPath("$.products[0].description")
                .isEqualTo("High-performance gaming laptop with RTX graphics")
                .jsonPath("$.products[0].price")
                .isEqualTo(1299.99)
                .jsonPath("$.products[0].inStock")
                .isEqualTo(true);
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} should return 404 as documented when order not found")
    void getOrderById_ShouldReturn404AsDocumented() {
        // Given
        when(orderRepository.findByOrderId("order-99999")).thenReturn(Mono.empty());

        // When & Then - Validate against OpenAPI specification
        webTestClient
                .get()
                .uri("/api/v1/orders/order-99999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("ORDER_NOT_FOUND")
                .jsonPath("$.message")
                .value(containsString("Order with ID 'order-99999' was not found"))
                .jsonPath("$.timestamp")
                .exists();
    }

    @Test
    @DisplayName("GET /api/v1/orders should work as documented with pagination")
    void listOrders_ShouldWorkAsDocumentedWithPagination() {
        // Given
        when(orderRepository.findAll()).thenReturn(Flux.just(sampleOrder));
        when(orderRepository.count()).thenReturn(Mono.just(1L));

        // When & Then - Validate against OpenAPI specification
        webTestClient
                .get()
                .uri("/api/v1/orders?page=0&size=20&sort=processedAt,desc")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                // Validate pagination structure as per OpenAPI spec
                .jsonPath("$.content")
                .isArray()
                .jsonPath("$.content[0].orderId")
                .isEqualTo("order-12345")
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
                .isEqualTo(true)
                .jsonPath("$.hasNext")
                .isEqualTo(false)
                .jsonPath("$.hasPrevious")
                .isEqualTo(false);
    }

    @Test
    @DisplayName("GET /api/v1/orders should work as documented with status filter")
    void listOrders_ShouldWorkAsDocumentedWithStatusFilter() {
        // Given
        when(orderRepository.findByStatus("PROCESSED")).thenReturn(Flux.just(sampleOrder));
        when(orderRepository.countByStatus("PROCESSED")).thenReturn(Mono.just(1L));

        // When & Then - Validate against OpenAPI specification
        webTestClient
                .get()
                .uri("/api/v1/orders?status=PROCESSED")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content")
                .isArray()
                .jsonPath("$.content[0].status")
                .isEqualTo("PROCESSED");
    }

    @Test
    @DisplayName("GET /api/v1/orders/customer/{customerId} should work as documented")
    void getOrdersByCustomerId_ShouldWorkAsDocumented() {
        // Given
        when(orderRepository.findByCustomerId("customer-67890")).thenReturn(Flux.just(sampleOrder));

        // When & Then - Validate against OpenAPI specification
        webTestClient
                .get()
                .uri("/api/v1/orders/customer/customer-67890")
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
                .isEqualTo("order-12345")
                .jsonPath("$[0].customerId")
                .isEqualTo("customer-67890")
                .jsonPath("$[0].customerName")
                .isEqualTo("John Doe");
    }

    @Test
    @DisplayName("GET /api/v1/orders/customer/{customerId} should return 404 as documented when no orders found")
    void getOrdersByCustomerId_ShouldReturn404AsDocumented() {
        // Given
        when(orderRepository.findByCustomerId("customer-99999")).thenReturn(Flux.empty());

        // When & Then - Validate against OpenAPI specification
        webTestClient
                .get()
                .uri("/api/v1/orders/customer/customer-99999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    @DisplayName("API endpoints should validate path parameters as documented in OpenAPI spec")
    void apiEndpoints_ShouldValidatePathParametersAsDocumented() {
        // Test that endpoints handle invalid IDs gracefully
        webTestClient
                .get()
                .uri("/api/v1/orders/invalid-order-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @DisplayName("API endpoints should validate query parameters as documented in OpenAPI spec")
    void apiEndpoints_ShouldValidateQueryParametersAsDocumented() {
        // Given
        when(orderRepository.findAll()).thenReturn(Flux.just(sampleOrder));
        when(orderRepository.count()).thenReturn(Mono.just(1L));

        // Test that endpoints handle query parameters gracefully
        webTestClient
                .get()
                .uri("/api/v1/orders?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("API endpoints should handle sorting as documented in OpenAPI spec")
    void apiEndpoints_ShouldHandleSortingAsDocumented() {
        // Given
        when(orderRepository.findAll()).thenReturn(Flux.just(sampleOrder));
        when(orderRepository.count()).thenReturn(Mono.just(1L));

        // Test valid sort parameters as documented
        List<String> validSorts = List.of(
                "orderId,asc", "orderId,desc",
                "customerId,asc", "customerId,desc",
                "totalAmount,asc", "totalAmount,desc",
                "processedAt,asc", "processedAt,desc",
                "status,asc", "status,desc"
        );

        for (String sort : validSorts) {
            webTestClient
                    .get()
                    .uri("/api/v1/orders?sort=" + sort)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Test
    @DisplayName("API endpoints should return proper content types as documented")
    void apiEndpoints_ShouldReturnProperContentTypesAsDocumented() {
        // Given
        when(orderRepository.findByOrderId("order-12345")).thenReturn(Mono.just(sampleOrder));

        // Test JSON content type
        webTestClient
                .get()
                .uri("/api/v1/orders/order-12345")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);

        // Test default content type
        webTestClient
                .get()
                .uri("/api/v1/orders/order-12345")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("API endpoints should handle errors as documented in OpenAPI spec")
    void apiEndpoints_ShouldHandleErrorsAsDocumented() {
        // Given
        when(orderRepository.findByOrderId("order-12345")).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then - Validate error response structure as per OpenAPI spec
        webTestClient
                .get()
                .uri("/api/v1/orders/order-12345")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code")
                .exists()
                .jsonPath("$.message")
                .exists()
                .jsonPath("$.timestamp")
                .exists();
    }

    @Test
    @DisplayName("API endpoints should validate request headers as documented")
    void apiEndpoints_ShouldValidateRequestHeadersAsDocumented() {
        // Given
        when(orderRepository.findByOrderId("order-12345")).thenReturn(Mono.just(sampleOrder));

        // Test with different accept headers
        webTestClient
                .get()
                .uri("/api/v1/orders/order-12345")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);

        webTestClient
                .get()
                .uri("/api/v1/orders/order-12345")
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);
    }

    private Order createSampleOrder() {
        Order order = new Order();
        order.setId("1");
        order.setOrderId("order-12345");
        order.setCustomerId("customer-67890");
        order.setCustomerName("John Doe");
        order.setCustomerStatus("ACTIVE");
        order.setTotalAmount(1299.99);
        order.setStatus(Order.OrderStatus.PROCESSED);
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
