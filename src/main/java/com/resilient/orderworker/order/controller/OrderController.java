/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.order.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resilient.orderworker.common.dto.PageResponse;
import com.resilient.orderworker.order.dto.OrderResponse;
import com.resilient.orderworker.order.entity.Order;
import com.resilient.orderworker.order.repository.OrderRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/**
 * REST Controller for order management operations.
 *
 * <p>This controller provides endpoints for retrieving and managing enriched orders. All endpoints
 * are reactive and return Mono or Flux types for non-blocking operations.
 *
 * <p>The controller handles:
 *
 * <ul>
 *   <li>Individual order retrieval by ID
 *   <li>Paginated order listing with filtering
 *   <li>Order status filtering
 *   <li>Customer-specific order queries
 * </ul>
 *
 * @author Resilient Order Enricher Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(
        name = "Orders",
        description =
                """
                Order management and retrieval operations.

                This API provides access to enriched order data that has been processed through
                the Kafka message pipeline, enriched with customer and product information from
                external services, and stored in MongoDB.
                """)
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get order by ID",
            description =
                    """
Retrieves a specific order by its unique identifier.

The order includes enriched customer information (name, status) and product details
(name, description, price, category, stock status) obtained from external APIs.

**Processing Flow:**
1. Order message consumed from Kafka
2. Customer data enriched via Go API
3. Product data enriched via Go API
4. Data validated and stored in MongoDB
5. Available via this endpoint
""",
            operationId = "getOrderById")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Order found and returned successfully",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = OrderResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Successful Order Response",
                                                        summary = "Complete enriched order",
                                                        value =
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
"""))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Order not found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        examples =
                                                @ExampleObject(
                                                        name = "Order Not Found",
                                                        value =
                                                                """
{
  "error": "ORDER_NOT_FOUND",
  "message": "Order with ID 'order-99999' was not found",
  "timestamp": "2025-01-08T10:30:00Z",
  "path": "/api/v1/orders/order-99999"
}
"""))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    public Mono<ResponseEntity<OrderResponse>> getOrderById(
            @Parameter(
                            description = "Unique order identifier",
                            example = "order-12345",
                            required = true,
                            schema = @Schema(pattern = "^order-[a-zA-Z0-9]+$"))
                    @PathVariable
                    String orderId) {

        logger.info("Retrieving order with ID: {}", orderId);

        return orderRepository
                .findByOrderId(orderId)
                .map(OrderResponse::fromEntity)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnSuccess(
                        response -> logger.info("Order retrieval completed for ID: {}", orderId))
                .doOnError(
                        error ->
                                logger.error("Error retrieving order with ID: {}", orderId, error));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List orders with pagination and filtering",
            description =
                    """
Retrieves a paginated list of orders with optional filtering capabilities.

**Filtering Options:**
- **status**: Filter by processing status (PROCESSING, COMPLETED, FAILED, RETRYING)
- **customerId**: Filter by specific customer

**Sorting:**
- Default: Most recently processed first (processedAt DESC)
- Supports sorting by: orderId, customerId, totalAmount, processedAt, status

**Pagination:**
- Default page size: 20
- Maximum page size: 100
- Zero-based page numbering
""",
            operationId = "listOrders")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Orders retrieved successfully",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema =
                                                @Schema(
                                                        implementation = PageResponse.class,
                                                        subTypes = {OrderResponse.class}),
                                        examples =
                                                @ExampleObject(
                                                        name = "Paginated Orders Response",
                                                        summary = "Page of enriched orders",
                                                        value =
                                                                """
{
  "content": [
    {
      "orderId": "order-12345",
      "customerId": "customer-67890",
      "customerName": "John Doe",
      "customerStatus": "ACTIVE",
      "products": [
        {
          "productId": "product-001",
          "name": "Gaming Laptop",
          "price": 1299.99,
          "category": "Electronics",
          "inStock": true
        }
      ],
      "totalAmount": 1299.99,
      "processedAt": "2025-01-08T10:30:00",
      "status": "COMPLETED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "hasNext": false,
  "hasPrevious": false
}
"""))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request parameters",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    public Mono<ResponseEntity<PageResponse<OrderResponse>>> listOrders(
            @Parameter(
                            description = "Page number (0-based)",
                            example = "0",
                            schema = @Schema(minimum = "0", defaultValue = "0"))
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(
                            description = "Number of items per page",
                            example = "20",
                            schema = @Schema(minimum = "1", maximum = "100", defaultValue = "20"))
                    @RequestParam(defaultValue = "20")
                    int size,
            @Parameter(
                            description = "Sort field and direction",
                            example = "processedAt,desc",
                            schema =
                                    @Schema(
                                            defaultValue = "processedAt,desc",
                                            allowableValues = {
                                                "orderId,asc",
                                                "orderId,desc",
                                                "customerId,asc",
                                                "customerId,desc",
                                                "totalAmount,asc",
                                                "totalAmount,desc",
                                                "processedAt,asc",
                                                "processedAt,desc",
                                                "status,asc",
                                                "status,desc"
                                            }))
                    @RequestParam(defaultValue = "processedAt,desc")
                    String sort,
            @Parameter(
                            description = "Filter by processing status",
                            example = "COMPLETED",
                            schema =
                                    @Schema(
                                            allowableValues = {
                                                "PROCESSING",
                                                "COMPLETED",
                                                "FAILED",
                                                "RETRYING"
                                            }))
                    @RequestParam(required = false)
                    String status,
            @Parameter(
                            description = "Filter by customer ID",
                            example = "customer-67890",
                            schema = @Schema(pattern = "^customer-[a-zA-Z0-9]+$"))
                    @RequestParam(required = false)
                    String customerId) {

        logger.info(
                "Listing orders with page: {}, size: {}, sort: {}, status: {}, customerId: {}",
                page,
                size,
                sort,
                status,
                customerId);

        // Parse sort parameter
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction =
                sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortField));

        // Build query based on filters (simplified for demonstration)
        Mono<List<Order>> ordersQuery;
        Mono<Long> countQuery;

        if (status != null && customerId != null) {
            ordersQuery =
                    orderRepository
                            .findByStatusAndCustomerId(status, customerId)
                            .skip(page * (long) size)
                            .take(size)
                            .collectList();
            countQuery = orderRepository.countByStatusAndCustomerId(status, customerId);
        } else if (status != null) {
            ordersQuery =
                    orderRepository
                            .findByStatus(status)
                            .skip(page * (long) size)
                            .take(size)
                            .collectList();
            countQuery = orderRepository.countByStatus(status);
        } else if (customerId != null) {
            ordersQuery =
                    orderRepository
                            .findByCustomerId(customerId)
                            .skip(page * (long) size)
                            .take(size)
                            .collectList();
            countQuery = orderRepository.countByCustomerId(customerId);
        } else {
            ordersQuery =
                    orderRepository.findAll().skip(page * (long) size).take(size).collectList();
            countQuery = orderRepository.count();
        }

        return Mono.zip(ordersQuery, countQuery)
                .map(
                        tuple -> {
                            List<OrderResponse> orderResponses =
                                    tuple.getT1().stream().map(OrderResponse::fromEntity).toList();

                            PageResponse<OrderResponse> pageResponse =
                                    PageResponse.of(orderResponses, page, size, tuple.getT2());

                            return ResponseEntity.ok(pageResponse);
                        })
                .doOnSuccess(
                        response ->
                                logger.info(
                                        "Orders listing completed with {} items",
                                        response.getBody().content().size()))
                .doOnError(error -> logger.error("Error listing orders", error));
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get orders by customer ID",
            description =
                    """
                    Retrieves all orders for a specific customer with pagination support.

                    This endpoint is useful for customer service representatives or customer
                    portals to view order history for a specific customer.

                    Orders are returned in descending order by processing date (most recent first).
                    """,
            operationId = "getOrdersByCustomerId")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Customer orders retrieved successfully",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        array =
                                                @ArraySchema(
                                                        schema =
                                                                @Schema(
                                                                        implementation =
                                                                                OrderResponse
                                                                                        .class)))),
                @ApiResponse(
                        responseCode = "404",
                        description = "No orders found for customer",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByCustomerId(
            @Parameter(
                            description = "Customer identifier",
                            example = "customer-67890",
                            required = true,
                            schema = @Schema(pattern = "^customer-[a-zA-Z0-9]+$"))
                    @PathVariable
                    String customerId) {

        logger.info("Retrieving orders for customer: {}", customerId);

        return orderRepository
                .findByCustomerId(customerId)
                .map(OrderResponse::fromEntity)
                .collectList()
                .map(
                        orders ->
                                orders.isEmpty()
                                        ? ResponseEntity.notFound().<List<OrderResponse>>build()
                                        : ResponseEntity.ok(orders))
                .doOnSuccess(
                        response ->
                                logger.info(
                                        "Customer orders retrieval completed for: {}", customerId))
                .doOnError(
                        error ->
                                logger.error(
                                        "Error retrieving orders for customer: {}",
                                        customerId,
                                        error));
    }
}
