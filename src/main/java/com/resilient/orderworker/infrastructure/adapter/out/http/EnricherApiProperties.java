/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.http;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Typed binding for outbound HTTP settings against the Go enrichment API. */
@ConfigurationProperties(prefix = "enricher-api")
public record EnricherApiProperties(@NotBlank String baseUrl, @NotNull Timeout timeout) {

    public EnricherApiProperties {
        if (timeout == null) {
            timeout = Timeout.defaults();
        }
    }

    public record Timeout(Duration connect, Duration read, Duration write, Duration response) {

        public static Timeout defaults() {
            return new Timeout(
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(30));
        }
    }
}
