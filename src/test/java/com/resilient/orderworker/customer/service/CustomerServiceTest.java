/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.customer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.resilient.orderworker.common.exception.CustomerNotFoundException;
import com.resilient.orderworker.common.exception.ExternalApiException;
import com.resilient.orderworker.customer.dto.CustomerResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Unit tests for CustomerService. */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private WebClient webClient;

    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock private WebClient.ResponseSpec responseSpec;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(webClient);
    }

    @Test
    void shouldGetCustomerSuccessfully() {
        // Given
        CustomerResponse expectedCustomer =
                new CustomerResponse("customer-1", "John Doe", "ACTIVE");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class))
                .thenReturn(Mono.just(expectedCustomer));

        // When & Then
        StepVerifier.create(customerService.getCustomer("customer-1"))
                .expectNext(expectedCustomer)
                .verifyComplete();
    }

    @Test
    void shouldThrowCustomerNotFoundWhenCustomerDoesNotExist() {
        // Given
        WebClientResponseException notFoundException =
                WebClientResponseException.create(
                        HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class))
                .thenReturn(Mono.error(notFoundException));

        // When & Then
        StepVerifier.create(customerService.getCustomer("customer-1"))
                .expectErrorMatches(throwable -> throwable instanceof CustomerNotFoundException)
                .verify();
    }

    @Test
    void shouldThrowExternalApiExceptionOnServerError() {
        // Given
        WebClientResponseException serverErrorException =
                WebClientResponseException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server Error", null, null, null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class))
                .thenReturn(Mono.error(serverErrorException));

        // When & Then
        StepVerifier.create(customerService.getCustomer("customer-1"))
                .expectErrorMatches(throwable -> throwable instanceof ExternalApiException)
                .verify();
    }

    @Test
    void shouldReturnTrueWhenCustomerIsValid() {
        // Given
        CustomerResponse activeCustomer = new CustomerResponse("customer-1", "John Doe", "ACTIVE");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class)).thenReturn(Mono.just(activeCustomer));

        // When & Then
        StepVerifier.create(customerService.isCustomerValid("customer-1"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenCustomerIsInactive() {
        // Given
        CustomerResponse inactiveCustomer =
                new CustomerResponse("customer-1", "John Doe", "INACTIVE");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class))
                .thenReturn(Mono.just(inactiveCustomer));

        // When & Then
        StepVerifier.create(customerService.isCustomerValid("customer-1"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenCustomerServiceFails() {
        // Given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        // When & Then
        StepVerifier.create(customerService.isCustomerValid("customer-1"))
                .expectNext(false)
                .verifyComplete();
    }
}
