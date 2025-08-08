/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.customer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.resilient.orderworker.common.exception.CustomerNotFoundException;
import com.resilient.orderworker.common.exception.ExternalApiException;
import com.resilient.orderworker.customer.dto.CustomerResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Mono;

/**
 * Service for fetching customer information from the Go API. Implements circuit breaker and retry
 * patterns for resilience.
 */
@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private static final String CIRCUIT_BREAKER_NAME = "customerService";
    private static final String RETRY_NAME = "customerService";

    private final WebClient webClient;

    public CustomerService(@Qualifier("enricherApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Fetches customer details by ID from the external Go API.
     *
     * @param customerId the customer ID
     * @return Mono containing customer response
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackGetCustomer")
    @Retry(name = RETRY_NAME)
    @Cacheable(value = "customers", key = "#customerId")
    public Mono<CustomerResponse> getCustomer(String customerId) {
        LOGGER.debug("Fetching customer details for ID: {}", customerId);

        return webClient
                .get()
                .uri("/v1/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerResponse.class)
                .doOnSuccess(
                        response -> LOGGER.debug("Successfully fetched customer: {}", customerId))
                .doOnError(
                        error ->
                                LOGGER.error(
                                        "Error fetching customer {}: {}",
                                        customerId,
                                        error.getMessage()))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    /**
     * Validates if a customer exists and is active.
     *
     * @param customerId the customer ID
     * @return Mono containing true if customer is valid and active
     */
    public Mono<Boolean> isCustomerValid(String customerId) {
        return getCustomer(customerId).map(CustomerResponse::isActive).onErrorReturn(false);
    }

    /** Fallback method for circuit breaker. */
    public Mono<CustomerResponse> fallbackGetCustomer(String customerId, Exception ex) {
        LOGGER.warn("Fallback triggered for customer {}: {}", customerId, ex.getMessage());
        return Mono.error(new ExternalApiException("Customer service temporarily unavailable", ex));
    }

    /** Maps WebClient exceptions to domain-specific exceptions. */
    private Throwable mapWebClientException(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new CustomerNotFoundException("Customer not found: " + ex.getMessage());
        }
        return new ExternalApiException("External API error: " + ex.getMessage(), ex);
    }
}
