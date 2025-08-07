/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.openapi;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * OpenAPI 3.0 configuration for the Resilient Order Enricher API.
 *
 * <p>This configuration sets up Swagger UI and API documentation with comprehensive metadata,
 * server information, and organized API tags for better documentation structure.
 *
 * <p>Swagger UI will be available at: {@code /swagger-ui.html} API docs JSON at: {@code
 * /v3/api-docs}
 *
 * @author Resilient Order Enricher Team
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${management.server.port:${server.port:8081}}")
    private String managementPort;

    /**
     * Configures the OpenAPI specification for the application.
     *
     * @return OpenAPI configuration with complete API metadata
     */
    @Bean
    public OpenAPI orderWorkerOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .tags(apiTags())
                .components(securityComponents());
    }

    /**
     * Creates comprehensive API information metadata.
     *
     * @return Info object with API details
     */
    private Info apiInfo() {
        return new Info()
                .title("Resilient Order Enricher API")
                .description(
                        """
**Resilient Order Processing System with Data Enrichment**

This API provides endpoints for managing and monitoring the order enrichment process.
The system consumes order messages from Kafka, enriches them with customer and product
data from external APIs, and stores the processed orders in MongoDB.

## Key Features

- **Order Management**: View and manage enriched orders
- **Health Monitoring**: Comprehensive health checks and metrics
- **Resilience Patterns**: Circuit breakers, retries, and distributed locking
- **Real-time Processing**: Reactive programming with WebFlux

## Architecture

- **Java 21** with **Spring Boot 3.3.5**
- **Spring WebFlux** for reactive programming
- **Apache Kafka** for message streaming
- **MongoDB** for document storage
- **Redis** for distributed locking and caching

## External Dependencies

- **Enricher API** (Go): Customer and product data enrichment
- **Kafka**: Order message consumption
- **MongoDB**: Persistent order storage
- **Redis**: Distributed coordination
""")
                .version("1.0.0")
                .contact(
                        new Contact()
                                .name("Resilient Order Enricher Team")
                                .email("support@resilient-order-enricher.com")
                                .url("https://github.com/alevelazcode/resilient-order-enricher"))
                .license(
                        new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Configures server information for different environments.
     *
     * @return List of server configurations
     */
    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                new Server()
                        .url("http://localhost:" + managementPort + "/actuator")
                        .description("Management & Monitoring Endpoints"),
                new Server()
                        .url("http://order-worker:8081")
                        .description("Docker Container Server"));
    }

    /**
     * Defines API tags for organized documentation structure.
     *
     * @return List of API tags
     */
    private List<Tag> apiTags() {
        return List.of(
                new Tag().name("Orders").description("Order management and retrieval operations"),
                new Tag()
                        .name("Health")
                        .description("Application health checks and status monitoring"),
                new Tag()
                        .name("Metrics")
                        .description("Application metrics and performance monitoring"),
                new Tag().name("Management").description("Actuator management endpoints"));
    }

    /**
     * Configures security components for future authentication implementation.
     *
     * <p>Currently defines placeholder security schemes that can be activated when authentication
     * is implemented. This provides a foundation for API key, JWT, or OAuth2 authentication.
     *
     * @return Components with security scheme definitions
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(
                        "apiKey",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API Key authentication (not currently implemented)"))
                .addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description(
                                        "JWT Bearer token authentication (not currently"
                                                + " implemented)"));
    }
}
