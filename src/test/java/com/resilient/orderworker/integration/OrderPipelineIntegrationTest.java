/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.resilient.orderworker.application.port.out.CustomerProvider;
import com.resilient.orderworker.application.port.out.OrderRepository;
import com.resilient.orderworker.application.port.out.ProductProvider;
import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.product.Product;
import com.resilient.orderworker.infrastructure.adapter.in.kafka.OrderMessagePayload;

import reactor.core.publisher.Mono;

/**
 * End-to-end integration test exercising the full pipeline: Kafka → OrderKafkaConsumer →
 * OrderProcessor → MongoDB, with Redis backing the distributed lock and failed-message store.
 *
 * <p>The Go enrichment API is stubbed via Mockito on the {@link CustomerProvider} and {@link
 * ProductProvider} ports so the test exercises the worker's wiring without depending on the Go
 * service.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@Testcontainers
@Tag("integration")
@DisplayName("Order pipeline integration test")
class OrderPipelineIntegrationTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"));

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "order_worker_itest");
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private OrderRepository orderRepository;

    @MockBean private CustomerProvider customerProvider;
    @MockBean private ProductProvider productProvider;

    @Test
    void consumes_enriches_andPersists() {
        when(customerProvider.getCustomer(any()))
                .thenReturn(Mono.just(new Customer("c1", "Alice", "ACTIVE")));
        when(productProvider.getProduct(any()))
                .thenReturn(
                        Mono.just(new Product("p1", "Laptop", "Gaming", new BigDecimal("999.99"))));

        OrderMessagePayload payload =
                new OrderMessagePayload(
                        "order-itest-1",
                        "c1",
                        List.of(new OrderMessagePayload.LinePayload("p1", 2)));

        kafkaTemplate.send("orders", payload.orderId(), payload);

        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () -> {
                            var order = orderRepository.findByOrderId("order-itest-1").block();
                            assertThat(order).as("order persisted in Mongo").isNotNull();
                            assertThat(order.orderId()).isEqualTo("order-itest-1");
                            assertThat(order.totalAmount())
                                    .isEqualByComparingTo(new BigDecimal("1999.98"));
                            assertThat(order.lines()).hasSize(1);
                        });
    }
}
