/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderWorkerOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Resilient Order Enricher API")
                                .description(
                                        "Order processing with enrichment, retry, and resilience patterns")
                                .version("1.0.0"));
    }
}
