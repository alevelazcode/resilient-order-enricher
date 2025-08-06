package com.resilient.orderworker.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO representing the order message received from Kafka.
 * Contains basic order information that needs to be enriched.
 */
public record OrderMessage(
    @JsonProperty("orderId")
    @NotBlank(message = "Order ID cannot be blank")
    String orderId,
    
    @JsonProperty("customerId")
    @NotBlank(message = "Customer ID cannot be blank")
    String customerId,
    
    @JsonProperty("products")
    @NotEmpty(message = "Products list cannot be empty")
    @Valid
    List<ProductInfo> products
) {
    
    /**
     * Basic product information from the order message.
     */
    public record ProductInfo(
        @JsonProperty("productId")
        @NotBlank(message = "Product ID cannot be blank")
        String productId,
        
        @JsonProperty("quantity")
        @NotNull(message = "Quantity cannot be null")
        Integer quantity
    ) {}
}