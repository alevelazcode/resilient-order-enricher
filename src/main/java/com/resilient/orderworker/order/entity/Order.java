/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.order.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB entity representing a processed order with enriched data from external APIs.
 *
 * <p>This entity represents the core domain object for order processing. It contains:
 *
 * <ul>
 *   <li><strong>Order Information</strong>: Order ID, customer details, and processing status
 *   <li><strong>Enriched Data</strong>: Customer name and status from external API
 *   <li><strong>Product Details</strong>: List of products with names, descriptions, and prices
 *   <li><strong>Processing Metadata</strong>: Timestamps, status, and total amounts
 * </ul>
 *
 * <p>The entity is designed to work with Spring Data MongoDB reactive repositories and includes
 * proper field mappings for MongoDB document storage.
 *
 * <p><strong>Example Usage:</strong>
 *
 * <pre>{@code
 * Order order = new Order();
 * order.setOrderId("order-12345");
 * order.setCustomerId("customer-67890");
 * order.setCustomerName("John Doe");
 * order.setCustomerStatus("ACTIVE");
 * order.setStatus(OrderStatus.PROCESSED);
 * order.setProcessedAt(LocalDateTime.now());
 * }</pre>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see OrderProduct
 * @see OrderStatus
 */
@Document(collection = "orders")
public class Order {

    /** Unique identifier for the order document in MongoDB. */
    @Id private String id;

    /** Unique order identifier from the business system. */
    @Field("orderId")
    private String orderId;

    /** Customer identifier from the business system. */
    @Field("customerId")
    private String customerId;

    /** Customer name retrieved from the enrichment API. */
    @Field("customerName")
    private String customerName;

    /** Customer status (e.g., "ACTIVE", "INACTIVE") from the enrichment API. */
    @Field("customerStatus")
    private String customerStatus;

    /** List of products in the order with enriched details. */
    @Field("products")
    private List<OrderProduct> products;

    /** Total amount of the order calculated from product prices and quantities. */
    @Field("totalAmount")
    private Double totalAmount;

    /** Timestamp when the order was processed by this system. */
    @Field("processedAt")
    private LocalDateTime processedAt;

    /** Current processing status of the order. */
    @Field("status")
    private OrderStatus status;

    /** Default constructor for Spring Data MongoDB. */
    public Order() {}

    /**
     * Constructs a new Order with the specified details.
     *
     * @param orderId unique order identifier
     * @param customerId customer identifier
     * @param customerName customer name from enrichment API
     * @param customerStatus customer status from enrichment API
     * @param products list of products in the order
     * @param totalAmount total order amount
     * @param status current processing status
     */
    public Order(
            final String orderId,
            final String customerId,
            final String customerName,
            final String customerStatus,
            final List<OrderProduct> products,
            final Double totalAmount,
            final OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerStatus = customerStatus;
        this.products = products;
        this.totalAmount = totalAmount;
        this.status = status;
        this.processedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    // Getters and Setters with JavaDoc
    /**
     * Gets the unique identifier for the order document in MongoDB.
     *
     * @return the MongoDB document ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the order document in MongoDB.
     *
     * @param id the MongoDB document ID to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the unique order identifier from the business system.
     *
     * @return the order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the unique order identifier from the business system.
     *
     * @param orderId the order ID to set
     */
    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the customer identifier from the business system.
     *
     * @return the customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer identifier from the business system.
     *
     * @param customerId the customer ID to set
     */
    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the customer name retrieved from the enrichment API.
     *
     * @return the customer name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the customer name retrieved from the enrichment API.
     *
     * @param customerName the customer name to set
     */
    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }

    /**
     * Gets the customer status from the enrichment API.
     *
     * @return the customer status (e.g., "ACTIVE", "INACTIVE")
     */
    public String getCustomerStatus() {
        return customerStatus;
    }

    /**
     * Sets the customer status from the enrichment API.
     *
     * @param customerStatus the customer status to set
     */
    public void setCustomerStatus(final String customerStatus) {
        this.customerStatus = customerStatus;
    }

    /**
     * Gets the list of products in the order with enriched details.
     *
     * @return the list of products
     */
    public List<OrderProduct> getProducts() {
        return products;
    }

    /**
     * Sets the list of products in the order with enriched details.
     *
     * @param products the list of products to set
     */
    public void setProducts(final List<OrderProduct> products) {
        this.products = products;
    }

    /**
     * Gets the total amount of the order calculated from product prices and quantities.
     *
     * @return the total order amount
     */
    public Double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the total amount of the order calculated from product prices and quantities.
     *
     * @param totalAmount the total order amount to set
     */
    public void setTotalAmount(final Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Gets the timestamp when the order was processed by this system.
     *
     * @return the processing timestamp
     */
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    /**
     * Sets the timestamp when the order was processed by this system.
     *
     * @param processedAt the processing timestamp to set
     */
    public void setProcessedAt(final LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    /**
     * Gets the current processing status of the order.
     *
     * @return the order status
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Sets the current processing status of the order.
     *
     * @param status the order status to set
     */
    public void setStatus(final OrderStatus status) {
        this.status = status;
    }

    /**
     * Nested class representing a product within an order.
     *
     * <p>This class contains enriched product information retrieved from external APIs, including
     * product details, pricing, and quantities.
     *
     * @author Alejandro Velazco
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class OrderProduct {
        /** Unique product identifier from the business system. */
        @Field("productId")
        private String productId;

        /** Product name retrieved from the enrichment API. */
        @Field("name")
        private String name;

        /** Product description retrieved from the enrichment API. */
        @Field("description")
        private String description;

        /** Product price retrieved from the enrichment API. */
        @Field("price")
        private Double price;

        /** Quantity of the product in the order. */
        @Field("quantity")
        private Integer quantity;

        /** Subtotal for this product (price * quantity). */
        @Field("subtotal")
        private Double subtotal;

        /** Default constructor for Spring Data MongoDB. */
        public OrderProduct() {}

        /**
         * Constructs a new OrderProduct with the specified details.
         *
         * @param productId unique product identifier
         * @param name product name from enrichment API
         * @param description product description from enrichment API
         * @param price product price from enrichment API
         * @param quantity quantity of the product in the order
         */
        public OrderProduct(
                final String productId,
                final String name,
                final String description,
                final Double price,
                final Integer quantity) {
            this.productId = productId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = price * quantity;
        }

        // Getters and Setters with JavaDoc
        /**
         * Gets the unique product identifier from the business system.
         *
         * @return the product ID
         */
        public String getProductId() {
            return productId;
        }

        /**
         * Sets the unique product identifier from the business system.
         *
         * @param productId the product ID to set
         */
        public void setProductId(final String productId) {
            this.productId = productId;
        }

        /**
         * Gets the product name retrieved from the enrichment API.
         *
         * @return the product name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the product name retrieved from the enrichment API.
         *
         * @param name the product name to set
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Gets the product description retrieved from the enrichment API.
         *
         * @return the product description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the product description retrieved from the enrichment API.
         *
         * @param description the product description to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }

        /**
         * Gets the product price retrieved from the enrichment API.
         *
         * @return the product price
         */
        public Double getPrice() {
            return price;
        }

        /**
         * Sets the product price retrieved from the enrichment API.
         *
         * @param price the product price to set
         */
        public void setPrice(final Double price) {
            this.price = price;
        }

        /**
         * Gets the quantity of the product in the order.
         *
         * @return the product quantity
         */
        public Integer getQuantity() {
            return quantity;
        }

        /**
         * Sets the quantity of the product in the order.
         *
         * @param quantity the product quantity to set
         */
        public void setQuantity(final Integer quantity) {
            this.quantity = quantity;
        }

        /**
         * Gets the subtotal for this product (price * quantity).
         *
         * @return the product subtotal
         */
        public Double getSubtotal() {
            return subtotal;
        }

        /**
         * Sets the subtotal for this product (price * quantity).
         *
         * @param subtotal the product subtotal to set
         */
        public void setSubtotal(final Double subtotal) {
            this.subtotal = subtotal;
        }
    }

    /**
     * Enumeration representing the processing status of an order.
     *
     * <p>This enum defines the possible states an order can be in during processing:
     *
     * <ul>
     *   <li><strong>PROCESSED</strong>: Order has been successfully processed and stored
     *   <li><strong>FAILED</strong>: Order processing failed and will not be retried
     *   <li><strong>RETRY</strong>: Order processing failed but will be retried
     * </ul>
     *
     * @author Alejandro Velazco
     * @version 1.0.0
     * @since 1.0.0
     */
    public enum OrderStatus {
        /** Order has been successfully processed and stored. */
        PROCESSED,
        /** Order processing failed and will not be retried. */
        FAILED,
        /** Order processing failed but will be retried. */
        RETRY
    }
}
