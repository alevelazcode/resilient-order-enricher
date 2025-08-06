package com.resilient.orderworker.product.service;

import com.resilient.orderworker.product.dto.ProductResponse;
import com.resilient.orderworker.common.exception.ProductNotFoundException;
import com.resilient.orderworker.common.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProductService.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;
    
    private ProductService productService;
    
    @BeforeEach
    void setUp() {
        productService = new ProductService(webClient);
    }
    
    @Test
    void shouldGetProductSuccessfully() {
        // Given
        ProductResponse expectedProduct = new ProductResponse("product-1", "Laptop", "Gaming laptop", 999.0);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductResponse.class)).thenReturn(Mono.just(expectedProduct));
        
        // When & Then
        StepVerifier.create(productService.getProduct("product-1"))
            .expectNext(expectedProduct)
            .verifyComplete();
    }
    
    @Test
    void shouldThrowProductNotFoundWhenProductDoesNotExist() {
        // Given
        WebClientResponseException notFoundException = 
            WebClientResponseException.create(HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductResponse.class)).thenReturn(Mono.error(notFoundException));
        
        // When & Then
        StepVerifier.create(productService.getProduct("product-1"))
            .expectErrorMatches(throwable -> throwable instanceof ProductNotFoundException)
            .verify();
    }
    
    @Test
    void shouldGetMultipleProductsSuccessfully() {
        // Given
        ProductResponse product1 = new ProductResponse("product-1", "Laptop", "Gaming laptop", 999.0);
        ProductResponse product2 = new ProductResponse("product-2", "Mouse", "Gaming mouse", 50.0);
        List<String> productIds = List.of("product-1", "product-2");
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductResponse.class))
            .thenReturn(Mono.just(product1))
            .thenReturn(Mono.just(product2));
        
        // When & Then
        StepVerifier.create(productService.getProducts(productIds))
            .expectNext(product1)
            .expectNext(product2)
            .verifyComplete();
    }
    
    @Test
    void shouldReturnTrueWhenAllProductsAreValid() {
        // Given
        ProductResponse validProduct1 = new ProductResponse("product-1", "Laptop", "Gaming laptop", 999.0);
        ProductResponse validProduct2 = new ProductResponse("product-2", "Mouse", "Gaming mouse", 50.0);
        List<String> productIds = List.of("product-1", "product-2");
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductResponse.class))
            .thenReturn(Mono.just(validProduct1))
            .thenReturn(Mono.just(validProduct2));
        
        // When & Then
        StepVerifier.create(productService.areProductsValid(productIds))
            .expectNext(true)
            .verifyComplete();
    }
    
    @Test
    void shouldReturnFalseWhenAnyProductIsInvalid() {
        // Given
        ProductResponse validProduct = new ProductResponse("product-1", "Laptop", "Gaming laptop", 999.0);
        ProductResponse invalidProduct = new ProductResponse("product-2", "", "Gaming mouse", 50.0);
        List<String> productIds = List.of("product-1", "product-2");
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductResponse.class))
            .thenReturn(Mono.just(validProduct))
            .thenReturn(Mono.just(invalidProduct));
        
        // When & Then
        StepVerifier.create(productService.areProductsValid(productIds))
            .expectNext(false)
            .verifyComplete();
    }
}