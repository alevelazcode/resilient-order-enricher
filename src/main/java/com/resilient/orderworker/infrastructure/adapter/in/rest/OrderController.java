/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.domain.exception.OrderNotFoundException;
import com.resilient.orderworker.domain.order.OrderStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Order management and retrieval")
public class OrderController {

    private final QueryOrdersUseCase queryOrders;

    public OrderController(QueryOrdersUseCase queryOrders) {
        this.queryOrders = queryOrders;
    }

    @Operation(summary = "Get order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}")
    public Mono<OrderResponse> getOrderById(@PathVariable String orderId) {
        return queryOrders
                .findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .map(OrderResponse::from);
    }

    @Operation(summary = "List orders with pagination and optional filters")
    @GetMapping
    public Mono<PageResponse<OrderResponse>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerId) {
        QueryOrdersUseCase.PageQuery query =
                new QueryOrdersUseCase.PageQuery(page, size, status, customerId);
        return queryOrders.findAll(query).map(p -> PageResponse.from(p, OrderResponse::from));
    }

    @Operation(summary = "Get orders by customer ID")
    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByCustomer(
            @PathVariable String customerId) {
        return queryOrders
                .findByCustomerId(customerId)
                .map(OrderResponse::from)
                .collectList()
                .map(
                        list ->
                                list.isEmpty()
                                        ? ResponseEntity.notFound().<List<OrderResponse>>build()
                                        : ResponseEntity.ok(list));
    }
}
