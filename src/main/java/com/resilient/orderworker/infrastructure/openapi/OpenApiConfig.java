/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * OpenAPI 3.0 configuration for the Resilient Order Enricher API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI specification for the application.
     *
     * @return OpenAPI configuration with complete API metadata
     */
    @Bean
    public OpenAPI orderWorkerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Resilient Order Enricher API")
                        .description("Order processing system with data enrichment and resilience patterns")
                        .version("1.0.0"));
    }
}
