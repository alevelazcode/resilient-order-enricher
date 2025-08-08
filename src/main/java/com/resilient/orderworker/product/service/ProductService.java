/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.product.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.resilient.orderworker.common.exception.ExternalApiException;
import com.resilient.orderworker.common.exception.ProductNotFoundException;
import com.resilient.orderworker.product.dto.ProductResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for fetching product information from the Go API. Implements circuit breaker and retry
 * patterns for resilience.
 */
@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private static final String CIRCUIT_BREAKER_NAME = "productService";
    private static final String RETRY_NAME = "productService";

    private final WebClient webClient;

    public ProductService(@Qualifier("enricherApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Fetches product details by ID from the external Go API.
     *
     * @param productId the product ID
     * @return Mono containing product response
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackGetProduct")
    @Retry(name = RETRY_NAME)
    @Cacheable(value = "products", key = "#productId")
    public Mono<ProductResponse> getProduct(String productId) {
        LOGGER.debug("Fetching product details for ID: {}", productId);

        return webClient
                .get()
                .uri("/v1/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .doOnSuccess(
                        response -> LOGGER.debug("Successfully fetched product: {}", productId))
                .doOnError(
                        error ->
                                LOGGER.error(
                                        "Error fetching product {}: {}",
                                        productId,
                                        error.getMessage()))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    /**
     * Fetches multiple products concurrently.
     *
     * @param productIds list of product IDs
     * @return Flux containing product responses
     */
    public Flux<ProductResponse> getProducts(List<String> productIds) {
        LOGGER.debug("Fetching {} products", productIds.size());

        return Flux.fromIterable(productIds)
                .flatMap(this::getProduct)
                .doOnComplete(
                        () -> LOGGER.debug("Completed fetching {} products", productIds.size()));
    }

    /**
     * Validates if all products exist and are valid.
     *
     * @param productIds list of product IDs
     * @return Mono containing true if all products are valid
     */
    public Mono<Boolean> areProductsValid(List<String> productIds) {
        return getProducts(productIds).all(ProductResponse::isValid).onErrorReturn(false);
    }

    /** Fallback method for circuit breaker. */
    public Mono<ProductResponse> fallbackGetProduct(String productId, Exception ex) {
        LOGGER.warn("Fallback triggered for product {}: {}", productId, ex.getMessage());
        return Mono.error(new ExternalApiException("Product service temporarily unavailable", ex));
    }

    /** Maps WebClient exceptions to domain-specific exceptions. */
    private Throwable mapWebClientException(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new ProductNotFoundException("Product not found: " + ex.getMessage());
        }
        return new ExternalApiException("External API error: " + ex.getMessage(), ex);
    }
}
