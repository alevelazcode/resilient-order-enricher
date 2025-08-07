/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilient.orderworker.config.TestConfig;

/**
 * Integration tests for OpenAPI/Swagger functionality.
 *
 * <p>These tests validate the complete OpenAPI/Swagger implementation including:
 *
 * <ul>
 *   <li>OpenAPI specification availability and structure
 *   <li>Swagger UI accessibility and functionality
 *   <li>API documentation completeness
 *   <li>Endpoint documentation accuracy
 *   <li>Schema validation
 * </ul>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DisplayName("OpenAPI/Swagger Integration Tests")
class OpenApiIntegrationTest {

    @Autowired private WebTestClient webTestClient;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("OpenAPI specification should be available at /v3/api-docs")
    void openApiSpecification_ShouldBeAvailable() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.openapi")
                .isEqualTo("3.0.1")
                .jsonPath("$.info.title")
                .isEqualTo("Resilient Order Enricher API")
                .jsonPath("$.info.description")
                .value(containsString("Order processing system with data enrichment"))
                .jsonPath("$.info.version")
                .isEqualTo("1.0.0")
                .jsonPath("$.servers")
                .isArray()
                .jsonPath("$.paths")
                .isMap()
                .jsonPath("$.components.schemas")
                .isMap();
    }

    @Test
    @DisplayName("OpenAPI specification should contain all required endpoints")
    void openApiSpecification_ShouldContainAllEndpoints() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.paths./api/v1/orders/{orderId}")
                .exists()
                .jsonPath("$.paths./api/v1/orders/{orderId}.get")
                .exists()
                .jsonPath("$.paths./api/v1/orders")
                .exists()
                .jsonPath("$.paths./api/v1/orders.get")
                .exists()
                .jsonPath("$.paths./api/v1/orders/customer/{customerId}")
                .exists()
                .jsonPath("$.paths./api/v1/orders/customer/{customerId}.get")
                .exists();
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper schemas")
    void openApiSpecification_ShouldContainProperSchemas() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.components.schemas.OrderResponse")
                .exists()
                .jsonPath("$.components.schemas.OrderResponse.properties.orderId")
                .exists()
                .jsonPath("$.components.schemas.OrderResponse.properties.customerId")
                .exists()
                .jsonPath("$.components.schemas.OrderResponse.properties.customerName")
                .exists()
                .jsonPath("$.components.schemas.OrderResponse.properties.products")
                .exists()
                .jsonPath("$.components.schemas.OrderResponse.properties.totalAmount")
                .exists()
                .jsonPath("$.components.schemas.OrderResponse.properties.status")
                .exists()
                .jsonPath("$.components.schemas.EnrichedProductResponse")
                .exists()
                .jsonPath("$.components.schemas.PageResponse")
                .exists();
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper tags")
    void openApiSpecification_ShouldContainProperTags() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.tags")
                .isArray()
                .jsonPath("$.tags[?(@.name == 'Orders')]")
                .exists();
    }

    @Test
    @DisplayName("Swagger UI should be accessible")
    void swaggerUi_ShouldBeAccessible() {
        webTestClient
                .get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .value("Location", containsString("swagger-ui/index.html"));
    }

    @Test
    @DisplayName("Swagger UI should redirect to correct location")
    void swaggerUi_ShouldRedirectToCorrectLocation() {
        webTestClient
                .get()
                .uri("/webjars/swagger-ui/index.html")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.TEXT_HTML);
    }

    @Test
    @DisplayName("Swagger UI configuration should be available")
    void swaggerUiConfiguration_ShouldBeAvailable() {
        webTestClient
                .get()
                .uri("/v3/api-docs/swagger-config")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.configUrl")
                .isEqualTo("/v3/api-docs/swagger-config")
                .jsonPath("$.url")
                .isEqualTo("/v3/api-docs")
                .jsonPath("$.validatorUrl")
                .isEqualTo("")
                .jsonPath("$.displayRequestDuration")
                .isEqualTo(true);
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper operation IDs")
    void openApiSpecification_ShouldContainProperOperationIds() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.operationId")
                .isEqualTo("getOrderById")
                .jsonPath("$.paths./api/v1/orders.get.operationId")
                .isEqualTo("listOrders")
                .jsonPath("$.paths./api/v1/orders/customer/{customerId}.get.operationId")
                .isEqualTo("getOrdersByCustomerId");
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper parameter definitions")
    void openApiSpecification_ShouldContainProperParameters() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.parameters[0].name")
                .isEqualTo("orderId")
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.parameters[0].in")
                .isEqualTo("path")
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.parameters[0].required")
                .isEqualTo(true)
                .jsonPath("$.paths./api/v1/orders.get.parameters[?(@.name == 'page')]")
                .exists()
                .jsonPath("$.paths./api/v1/orders.get.parameters[?(@.name == 'size')]")
                .exists()
                .jsonPath("$.paths./api/v1/orders.get.parameters[?(@.name == 'status')]")
                .exists();
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper response definitions")
    void openApiSpecification_ShouldContainProperResponses() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.responses.200")
                .exists()
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.responses.404")
                .exists()
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.responses.500")
                .exists()
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.responses.200.description")
                .value(containsString("Order found and returned successfully"))
                .jsonPath("$.paths./api/v1/orders/{orderId}.get.responses.404.description")
                .value(containsString("Order not found"));
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper examples")
    void openApiSpecification_ShouldContainProperExamples() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath(
                        "$.paths./api/v1/orders/{orderId}.get.responses.200.content.application/json.examples")
                .exists()
                .jsonPath(
                        "$.paths./api/v1/orders/{orderId}.get.responses.200.content.application/json.examples['Successful"
                            + " Order Response']")
                .exists()
                .jsonPath(
                        "$.paths./api/v1/orders/{orderId}.get.responses.200.content.application/json.examples['Successful"
                            + " Order Response'].summary")
                .isEqualTo("Complete enriched order");
    }

    @Test
    @DisplayName("OpenAPI specification should be valid JSON")
    void openApiSpecification_ShouldBeValidJson() {
        String responseBody =
                webTestClient
                        .get()
                        .uri("/v3/api-docs")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody(String.class)
                        .returnResult()
                        .getResponseBody();

        assertThat(responseBody).isNotNull();

        // Verify it can be parsed as JSON
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = objectMapper.readValue(responseBody, Map.class);
            assertThat(jsonMap).isNotNull();
            assertThat(jsonMap).containsKey("openapi");
            assertThat(jsonMap).containsKey("info");
            assertThat(jsonMap).containsKey("paths");
            assertThat(jsonMap).containsKey("components");
        } catch (Exception e) {
            throw new AssertionError("OpenAPI specification is not valid JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper server information")
    void openApiSpecification_ShouldContainProperServerInfo() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.servers")
                .isArray()
                .jsonPath("$.servers[0]")
                .exists()
                .jsonPath("$.servers[0].description")
                .isEqualTo("Generated server url");
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper schema validation")
    void openApiSpecification_ShouldContainProperSchemaValidation() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.components.schemas.OrderResponse.properties.orderId.pattern")
                .isEqualTo("^order-[a-zA-Z0-9]+$")
                .jsonPath("$.components.schemas.OrderResponse.properties.customerId.pattern")
                .isEqualTo("^customer-[a-zA-Z0-9]+$")
                .jsonPath("$.components.schemas.OrderResponse.properties.totalAmount.minimum")
                .isEqualTo(0)
                .jsonPath("$.components.schemas.EnrichedProductResponse.properties.price.minimum")
                .isEqualTo(0);
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper enum values")
    void openApiSpecification_ShouldContainProperEnums() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.components.schemas.OrderResponse.properties.status.enum")
                .isArray()
                .jsonPath("$.components.schemas.OrderResponse.properties.customerStatus.enum")
                .isArray()
                .jsonPath("$.components.schemas.OrderResponse.properties.status.enum")
                .value(hasSize(4))
                .jsonPath("$.components.schemas.OrderResponse.properties.customerStatus.enum")
                .value(hasSize(3));
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper content types")
    void openApiSpecification_ShouldContainProperContentTypes() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath(
                        "$.paths./api/v1/orders/{orderId}.get.responses.200.content.application/json")
                .exists()
                .jsonPath(
                        "$.paths./api/v1/orders/{orderId}.get.responses.404.content.application/json")
                .exists()
                .jsonPath(
                        "$.paths./api/v1/orders/{orderId}.get.responses.500.content.application/json")
                .exists();
    }

    @Test
    @DisplayName("OpenAPI specification should contain proper security definitions")
    void openApiSpecification_ShouldContainProperSecurityDefinitions() {
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.components")
                .exists();
    }

    @Test
    @DisplayName("OpenAPI specification should be accessible with different accept headers")
    void openApiSpecification_ShouldBeAccessibleWithDifferentAcceptHeaders() {
        // Test with application/json
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);

        // Test with */* (default)
        webTestClient
                .get()
                .uri("/v3/api-docs")
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Swagger UI should load all required resources")
    void swaggerUi_ShouldLoadAllRequiredResources() {
        // Test main Swagger UI page
        webTestClient
                .get()
                .uri("/webjars/swagger-ui/index.html")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.TEXT_HTML);

        // Test Swagger UI CSS
        webTestClient
                .get()
                .uri("/webjars/swagger-ui/swagger-ui.css")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.valueOf("text/css"));

        // Test Swagger UI JavaScript
        webTestClient
                .get()
                .uri("/webjars/swagger-ui/swagger-ui-bundle.js")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.valueOf("application/javascript"));
    }
}
