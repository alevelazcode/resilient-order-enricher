/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.http;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    private static final int MAX_IN_MEMORY_SIZE = 1024 * 1024;

    /**
     * The injected {@link WebClient.Builder} is the Spring-Boot-managed builder that already has
     * the Micrometer observation and tracing registries wired in, so HTTP calls produce spans and
     * propagate trace headers automatically when {@code micrometer-tracing-bridge-*} is on the
     * classpath.
     */
    @Bean("enricherApiWebClient")
    public WebClient enricherApiWebClient(
            WebClient.Builder builder, EnricherApiProperties properties) {
        EnricherApiProperties.Timeout t = properties.timeout();
        HttpClient httpClient =
                HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) t.connect().toMillis())
                        .responseTimeout(t.response())
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                t.read().toMillis(),
                                                                TimeUnit.MILLISECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                t.write().toMillis(),
                                                                TimeUnit.MILLISECONDS)));

        return builder.baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();
    }
}
