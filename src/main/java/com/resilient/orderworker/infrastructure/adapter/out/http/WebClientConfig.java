/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.http;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${enricher-api.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${enricher-api.timeout.connect:5000}")
    private int connectTimeoutMs;

    @Value("${enricher-api.timeout.read:10000}")
    private int readTimeoutMs;

    @Value("${enricher-api.timeout.write:10000}")
    private int writeTimeoutMs;

    @Value("${enricher-api.timeout.response:30000}")
    private long responseTimeoutMs;

    @Bean("enricherApiWebClient")
    public WebClient enricherApiWebClient() {
        HttpClient httpClient =
                HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                        .responseTimeout(Duration.ofMillis(responseTimeoutMs))
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                readTimeoutMs,
                                                                TimeUnit.MILLISECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                writeTimeoutMs,
                                                                TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();
    }
}
