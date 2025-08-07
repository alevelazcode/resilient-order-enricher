/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Resilient Order Worker.
 *
 * <p>This application processes orders from Kafka, enriches them with external APIs, and stores the
 * processed data in MongoDB with resilience patterns including circuit breakers, retries, and
 * distributed locking.
 *
 * <p>The application implements a feature-based architecture with the following key components:
 *
 * <ul>
 *   <li><strong>Order Processing</strong>: Kafka message consumption and order enrichment
 *   <li><strong>Data Enrichment</strong>: External API calls to Go services for customer and
 *       product data
 *   <li><strong>Persistence</strong>: MongoDB storage with reactive programming
 *   <li><strong>Resilience</strong>: Circuit breakers, exponential retries, and distributed locking
 *   <li><strong>Monitoring</strong>: Health checks, metrics, and observability
 * </ul>
 *
 * <p><strong>Key Features:</strong>
 *
 * <ul>
 *   <li>Reactive programming with Spring WebFlux
 *   <li>Kafka message processing with consumer groups
 *   <li>MongoDB reactive repositories
 *   <li>Redis-based distributed locking
 *   <li>Circuit breaker patterns with Resilience4j
 *   <li>Comprehensive error handling and retry logic
 * </ul>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="https://spring.io/projects/spring-boot">Spring Boot</a>
 * @see <a href="https://spring.io/projects/spring-kafka">Spring Kafka</a>
 * @see <a href="https://spring.io/projects/spring-data-mongodb">Spring Data MongoDB</a>
 */
@SpringBootApplication
@EnableKafka
@EnableReactiveMongoRepositories
@EnableScheduling
public class OrderWorkerApplication {

    /**
     * Main method to start the Resilient Order Worker application.
     *
     * <p>This method initializes the Spring Boot application context and starts the embedded web
     * server. The application will:
     *
     * <ul>
     *   <li>Connect to Kafka and start consuming messages
     *   <li>Initialize MongoDB reactive repositories
     *   <li>Set up Redis connections for distributed locking
     *   <li>Start health check endpoints
     *   <li>Initialize circuit breakers and retry mechanisms
     * </ul>
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderWorkerApplication.class, args);
    }
}
