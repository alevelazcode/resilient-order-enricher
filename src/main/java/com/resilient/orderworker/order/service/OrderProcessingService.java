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
 * Main service for processing orders with comprehensive enrichment, validation, and storage
 * capabilities.
 *
 * <p>This service orchestrates the complete order processing workflow, including:
 *
 * <ul>
 *   <li><strong>Distributed Locking</strong>: Prevents duplicate processing of the same order
 *   <li><strong>Data Enrichment</strong>: Retrieves customer and product details from external APIs
 *   <li><strong>Validation</strong>: Ensures data integrity and business rules compliance
 *   <li><strong>Persistence</strong>: Stores enriched orders in MongoDB
 *   <li><strong>Error Handling</strong>: Implements comprehensive error handling and logging
 * </ul>
 *
 * <p>The service uses reactive programming patterns with Spring WebFlux and implements resilience
 * patterns including circuit breakers, retries, and distributed locking.
 *
 * <p><strong>Processing Flow:</strong>
 *
 * <ol>
 *   <li>Acquire distributed lock for order ID
 *   <li>Check if order already exists (idempotency)
 *   <li>Enrich customer data from external API
 *   <li>Enrich product data from external API (parallel)
 *   <li>Validate enriched data
 *   <li>Build and save order to MongoDB
 *   <li>Release distributed lock
 * </ol>
 *
 * <p><strong>Example Usage:</strong>
 *
 * <pre>{@code
 * OrderMessage message = new OrderMessage("order-123", "customer-456", products);
 * Order processedOrder = orderProcessingService.processOrder(message)
 *     .doOnSuccess(order -> log.info("Order processed: {}", order.getOrderId()))
 *     .doOnError(error -> log.error("Processing failed: {}", error.getMessage()))
 *     .block();
 * }</pre>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see OrderMessage
 * @see Order
 * @see CustomerService
 * @see ProductService
 * @see DistributedLockService
 */
@Service
public class OrderProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final DistributedLockService lockService;

    /**
     * Constructs a new OrderProcessingService with the required dependencies.
     *
     * @param orderRepository repository for order persistence operations
     * @param customerService service for customer data enrichment
     * @param productService service for product data enrichment
     * @param lockService service for distributed locking
     */
    public OrderProcessingService(
            final OrderRepository orderRepository,
            final CustomerService customerService,
            final ProductService productService,
            final DistributedLockService lockService) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.lockService = lockService;
    }

    /**
     * Processes an order message with distributed locking to prevent duplicate processing.
     *
     * <p>This method implements the main order processing workflow:
     *
     * <ul>
     *   <li>Acquires a distributed lock using the order ID
     *   <li>Checks for existing order to ensure idempotency
     *   <li>Enriches order data with customer and product information
     *   <li>Validates the enriched data
     *   <li>Persists the order to MongoDB
     *   <li>Releases the distributed lock
     * </ul>
     *
     * <p>The method uses reactive programming patterns and returns a {@code Mono<Order>} that
     * completes when the order processing is finished.
     *
     * @param orderMessage the order message to process
     * @return Mono containing the processed order
     * @throws OrderProcessingException if order processing fails
     * @see OrderMessage
     * @see Order
     */
    public Mono<Order> processOrder(final OrderMessage orderMessage) {
        logger.info("Starting to process order: {}", orderMessage.orderId());

        return lockService.executeWithLock(
                orderMessage.orderId(), () -> processOrderInternal(orderMessage));
    }

    /**
     * Internal order processing logic without distributed locking.
     *
     * <p>This method handles the core order processing logic:
     *
     * <ul>
     *   <li>Checks if the order already exists to prevent duplicates
     *   <li>If order exists, returns the existing order
     *   <li>If order doesn't exist, enriches and saves the order
     * </ul>
     *
     * @param orderMessage the order message to process
     * @return Mono containing the processed order
     */
    private Mono<Order> processOrderInternal(final OrderMessage orderMessage) {
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

    /**
     * Enriches order data with customer and product information and saves to MongoDB.
     *
     * <p>This method performs the following operations:
     *
     * <ul>
     *   <li>Fetches customer data from external API
     *   <li>Fetches product data from external API (in parallel)
     *   <li>Validates the enriched data
     *   <li>Builds the complete order entity
     *   <li>Saves the order to MongoDB
     * </ul>
     *
     * @param orderMessage the order message to enrich and save
     * @return Mono containing the saved order
     */
    private Mono<Order> enrichAndSaveOrder(final OrderMessage orderMessage) {
        logger.debug("Enriching order data for: {}", orderMessage.orderId());

        // Fetch customer and products in parallel
        Mono<CustomerResponse> customerMono =
                customerService.getCustomer(orderMessage.customerId());

        List<String> productIds =
                orderMessage.products().stream()
                        .map(OrderMessage.ProductInfo::productId)
                        .collect(Collectors.toList());

        Flux<ProductResponse> productsFlux =
                Flux.fromIterable(productIds).flatMap(productService::getProduct);

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
