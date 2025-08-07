/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.resilient.orderworker.order.entity.Order;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for order information exposed via REST API.
 *
 * <p>This record represents the complete order information including enriched customer and product
 * data. It's used for API responses and includes comprehensive validation and documentation.
 *
 * @param orderId Unique identifier for the order
 * @param customerId Customer identifier
 * @param customerName Enriched customer name
 * @param customerStatus Customer status (ACTIVE, INACTIVE, etc.)
 * @param products List of enriched products in the order
 * @param totalAmount Calculated total amount for the order
 * @param processedAt Timestamp when the order was processed
 * @param status Current order processing status
 * @author Resilient Order Enricher Team
 * @since 1.0.0
 */
@Schema(
        description = "Enriched order information with customer and product details",
        example =
                """
                {
                  "orderId": "order-12345",
                  "customerId": "customer-67890",
                  "customerName": "John Doe",
                  "customerStatus": "ACTIVE",
                  "products": [
                    {
                      "productId": "product-001",
                      "name": "Gaming Laptop",
                      "description": "High-performance gaming laptop with RTX graphics",
                      "price": 1299.99,
                      "category": "Electronics",
                      "inStock": true
                    }
                  ],
                  "totalAmount": 1299.99,
                  "processedAt": "2025-01-08T10:30:00",
                  "status": "COMPLETED"
                }
                """)
public record OrderResponse(
        @Schema(
                        description = "Unique identifier for the order",
                        example = "order-12345",
                        pattern = "^order-[a-zA-Z0-9]+$")
                @JsonProperty("orderId")
                String orderId,
        @Schema(
                        description = "Customer identifier",
                        example = "customer-67890",
                        pattern = "^customer-[a-zA-Z0-9]+$")
                @JsonProperty("customerId")
                String customerId,
        @Schema(description = "Enriched customer name from external API", example = "John Doe")
                @JsonProperty("customerName")
                String customerName,
        @Schema(
                        description = "Customer account status",
                        example = "ACTIVE",
                        allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
                @JsonProperty("customerStatus")
                String customerStatus,
        @Schema(description = "List of enriched products in the order", minProperties = 1)
                @JsonProperty("products")
                List<EnrichedProductResponse> products,
        @Schema(
                        description = "Total calculated amount for all products in the order",
                        example = "1299.99",
                        minimum = "0")
                @JsonProperty("totalAmount")
                BigDecimal totalAmount,
        @Schema(
                        description = "Timestamp when the order was processed and enriched",
                        example = "2025-01-08T10:30:00",
                        type = "string",
                        format = "date-time")
                @JsonProperty("processedAt")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                LocalDateTime processedAt,
        @Schema(
                        description = "Current processing status of the order",
                        example = "COMPLETED",
                        allowableValues = {"PROCESSING", "COMPLETED", "FAILED", "RETRYING"})
                @JsonProperty("status")
                String status) {

    /**
     * Creates an OrderResponse from an Order entity.
     *
     * @param order The order entity to convert
     * @return OrderResponse with all order details
     */
    public static OrderResponse fromEntity(Order order) {
        List<EnrichedProductResponse> productResponses =
                order.getProducts().stream().map(EnrichedProductResponse::fromEntity).toList();

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getCustomerStatus(),
                productResponses,
                BigDecimal.valueOf(order.getTotalAmount()),
                order.getProcessedAt(),
                order.getStatus().toString());
    }

    /**
     * Enriched product information within an order response.
     *
     * @param productId Unique product identifier
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param category Product category
     * @param inStock Stock availability status
     */
    @Schema(description = "Enriched product information within an order")
    public record EnrichedProductResponse(
            @Schema(
                            description = "Unique product identifier",
                            example = "product-001",
                            pattern = "^product-[a-zA-Z0-9]+$")
                    @JsonProperty("productId")
                    String productId,
            @Schema(description = "Product name", example = "Gaming Laptop") @JsonProperty("name")
                    String name,
            @Schema(
                            description = "Detailed product description",
                            example = "High-performance gaming laptop with RTX graphics")
                    @JsonProperty("description")
                    String description,
            @Schema(description = "Product price", example = "1299.99", minimum = "0")
                    @JsonProperty("price")
                    BigDecimal price,
            @Schema(description = "Product category", example = "Electronics")
                    @JsonProperty("category")
                    String category,
            @Schema(description = "Stock availability status", example = "true")
                    @JsonProperty("inStock")
                    Boolean inStock) {

        /**
         * Creates an EnrichedProductResponse from an Order.OrderProduct entity.
         *
         * @param product The order product entity
         * @return EnrichedProductResponse with product details
         */
        public static EnrichedProductResponse fromEntity(Order.OrderProduct product) {
            return new EnrichedProductResponse(
                    product.getProductId(),
                    product.getName(),
                    product.getDescription(),
                    BigDecimal.valueOf(product.getPrice()),
                    "General", // Default category since not in OrderProduct
                    true); // Default to in stock since not in OrderProduct
        }
    }
}
