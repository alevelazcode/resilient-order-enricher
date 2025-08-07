/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.order.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.resilient.orderworker.common.exception.OrderProcessingException;
import com.resilient.orderworker.customer.dto.CustomerResponse;
import com.resilient.orderworker.customer.service.CustomerService;
import com.resilient.orderworker.infrastructure.redis.DistributedLockService;
import com.resilient.orderworker.order.dto.OrderMessage;
import com.resilient.orderworker.order.entity.Order;
import com.resilient.orderworker.order.repository.OrderRepository;
import com.resilient.orderworker.product.dto.ProductResponse;
import com.resilient.orderworker.product.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Main service for processing orders. Coordinates enrichment, validation, and storage of orders.
 */
@Service
public class OrderProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final DistributedLockService lockService;

    public OrderProcessingService(
            OrderRepository orderRepository,
            CustomerService customerService,
            ProductService productService,
            DistributedLockService lockService) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.lockService = lockService;
    }

    /**
     * Processes an order message with distributed locking.
     *
     * @param orderMessage the order message to process
     * @return Mono containing the processed order
     */
    public Mono<Order> processOrder(OrderMessage orderMessage) {
        logger.info("Starting to process order: {}", orderMessage.orderId());

        return lockService.executeWithLock(
                orderMessage.orderId(), () -> processOrderInternal(orderMessage));
    }

    /** Internal order processing logic. */
    private Mono<Order> processOrderInternal(OrderMessage orderMessage) {
        // Check if order already exists
        return orderRepository
                .existsByOrderId(orderMessage.orderId())
                .flatMap(
                        exists -> {
                            if (exists) {
                                logger.warn("Order already processed: {}", orderMessage.orderId());
                                return orderRepository.findByOrderId(orderMessage.orderId());
                            }
                            return enrichAndSaveOrder(orderMessage);
                        })
                .doOnSuccess(
                        order ->
                                logger.info(
                                        "Successfully processed order: {}", orderMessage.orderId()))
                .doOnError(
                        error ->
                                logger.error(
                                        "Error processing order {}: {}",
                                        orderMessage.orderId(),
                                        error.getMessage()));
    }

    /** Enriches order data and saves to MongoDB. */
    private Mono<Order> enrichAndSaveOrder(OrderMessage orderMessage) {
        logger.debug("Enriching order data for: {}", orderMessage.orderId());

        // Fetch customer and products in parallel
        Mono<CustomerResponse> customerMono =
                customerService.getCustomer(orderMessage.customerId());

        List<String> productIds =
                orderMessage.products().stream()
                        .map(OrderMessage.ProductInfo::productId)
                        .collect(Collectors.toList());

        Flux<ProductResponse> productsFlux = productService.getProducts(productIds);

        return Mono.zip(customerMono, productsFlux.collectList())
                .flatMap(
                        tuple -> {
                            CustomerResponse customer = tuple.getT1();
                            List<ProductResponse> products = tuple.getT2();

                            return validateAndBuildOrder(orderMessage, customer, products);
                        })
                .flatMap(orderRepository::save);
    }

    /** Validates enriched data and builds the order entity. */
    private Mono<Order> validateAndBuildOrder(
            OrderMessage orderMessage, CustomerResponse customer, List<ProductResponse> products) {

        // Validate customer is active
        if (!customer.isActive()) {
            return Mono.error(
                    new OrderProcessingException(
                            "Customer is not active: " + customer.customerId()));
        }

        // Validate all products exist and are valid
        boolean allProductsValid = products.stream().allMatch(ProductResponse::isValid);
        if (!allProductsValid) {
            return Mono.error(
                    new OrderProcessingException(
                            "One or more products are invalid for order: "
                                    + orderMessage.orderId()));
        }

        // Build order products with quantities
        List<Order.OrderProduct> orderProducts =
                orderMessage.products().stream()
                        .map(
                                productInfo -> {
                                    ProductResponse product =
                                            products.stream()
                                                    .filter(
                                                            p ->
                                                                    p.productId()
                                                                            .equals(
                                                                                    productInfo
                                                                                            .productId()))
                                                    .findFirst()
                                                    .orElseThrow(
                                                            () ->
                                                                    new OrderProcessingException(
                                                                            "Product not found: "
                                                                                    + productInfo
                                                                                            .productId()));

                                    return new Order.OrderProduct(
                                            product.productId(),
                                            product.name(),
                                            product.description(),
                                            product.price(),
                                            productInfo.quantity());
                                })
                        .collect(Collectors.toList());

        // Calculate total amount
        double totalAmount =
                orderProducts.stream().mapToDouble(Order.OrderProduct::getSubtotal).sum();

        // Create order entity
        Order order =
                new Order(
                        orderMessage.orderId(),
                        customer.customerId(),
                        customer.name(),
                        customer.status(),
                        orderProducts,
                        totalAmount,
                        Order.OrderStatus.PROCESSED);

        logger.debug(
                "Built order with total amount: {} for order: {}",
                totalAmount,
                orderMessage.orderId());

        return Mono.just(order);
    }
}
