/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.http;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.resilient.orderworker.application.port.out.CustomerProvider;
import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.exception.CustomerNotFoundException;
import com.resilient.orderworker.domain.exception.ExternalServiceException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Component
public class CustomerHttpAdapter implements CustomerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerHttpAdapter.class);
    private static final String INSTANCE = "customerService";

    private final WebClient webClient;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final Cache<String, Customer> cache;
    private Retry retry;
    private TimeLimiter timeLimiter;

    public CustomerHttpAdapter(
            @Qualifier("enricherApiWebClient") WebClient webClient,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry) {
        this.webClient = webClient;
        this.retryRegistry = retryRegistry;
        this.timeLimiterRegistry = timeLimiterRegistry;
        this.cache =
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(15))
                        .build();
    }

    @PostConstruct
    void init() {
        this.retry = retryRegistry.retry(INSTANCE);
        this.timeLimiter = timeLimiterRegistry.timeLimiter(INSTANCE);
    }

    @Override
    @CircuitBreaker(name = INSTANCE)
    public Mono<Customer> getCustomer(String customerId) {
        Customer cached = cache.getIfPresent(customerId);
        if (cached != null) {
            return Mono.just(cached);
        }
        return fetch(customerId)
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .transformDeferred(RetryOperator.of(retry))
                .doOnNext(c -> cache.put(customerId, c));
    }

    private Mono<Customer> fetch(String customerId) {
        return webClient
                .get()
                .uri("/v1/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .map(CustomerDto::toDomain)
                .onErrorMap(WebClientResponseException.class, this::mapError)
                .doOnError(
                        err ->
                                LOG.warn(
                                        "Customer fetch failed for {}: {}",
                                        customerId,
                                        err.toString()));
    }

    private Throwable mapError(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new CustomerNotFoundException(ex.getMessage());
        }
        return new ExternalServiceException("Customer API error: " + ex.getStatusCode(), ex);
    }
}
