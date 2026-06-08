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
import com.resilient.orderworker.application.port.out.ProductProvider;
import com.resilient.orderworker.domain.exception.ExternalServiceException;
import com.resilient.orderworker.domain.exception.ProductNotFoundException;
import com.resilient.orderworker.domain.product.Product;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Component
public class ProductHttpAdapter implements ProductProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ProductHttpAdapter.class);
    private static final String INSTANCE = "productService";

    private final WebClient webClient;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final Cache<String, Product> cache;
    private Retry retry;
    private TimeLimiter timeLimiter;
    private Bulkhead bulkhead;

    public ProductHttpAdapter(
            @Qualifier("enricherApiWebClient") WebClient webClient,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry,
            BulkheadRegistry bulkheadRegistry) {
        this.webClient = webClient;
        this.retryRegistry = retryRegistry;
        this.timeLimiterRegistry = timeLimiterRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
        this.cache =
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .build();
    }

    @PostConstruct
    void init() {
        this.retry = retryRegistry.retry(INSTANCE);
        this.timeLimiter = timeLimiterRegistry.timeLimiter(INSTANCE);
        this.bulkhead = bulkheadRegistry.bulkhead(INSTANCE);
    }

    @Override
    @CircuitBreaker(name = INSTANCE)
    public Mono<Product> getProduct(String productId) {
        Product cached = cache.getIfPresent(productId);
        if (cached != null) {
            return Mono.just(cached);
        }
        return fetch(productId)
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .transformDeferred(RetryOperator.of(retry))
                .doOnNext(p -> cache.put(productId, p));
    }

    private Mono<Product> fetch(String productId) {
        return webClient
                .get()
                .uri("/v1/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .map(ProductDto::toDomain)
                .onErrorMap(WebClientResponseException.class, this::mapError)
                .doOnError(
                        err ->
                                LOG.warn(
                                        "Product fetch failed for {}: {}",
                                        productId,
                                        err.toString()));
    }

    private Throwable mapError(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new ProductNotFoundException(ex.getMessage());
        }
        return new ExternalServiceException("Product API error: " + ex.getStatusCode(), ex);
    }
}
