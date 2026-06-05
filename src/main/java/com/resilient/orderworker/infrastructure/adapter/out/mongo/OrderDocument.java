/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "orders")
public class OrderDocument {

    // Indexes (including the unique orderId index) are declared in MongoIndexInitializer
    // so they are guaranteed to be created without auto-index-creation.
    @Id private String id;

    @Field("orderId")
    private String orderId;

    @Field("customerId")
    private String customerId;

    @Field("customerName")
    private String customerName;

    @Field("customerStatus")
    private String customerStatus;

    @Field("lines")
    private List<OrderLineDocument> lines;

    @Field("totalAmount")
    private BigDecimal totalAmount;

    @Field("processedAt")
    private Instant processedAt;

    @Field("status")
    private String status;

    public OrderDocument() {}

    public OrderDocument(
            String orderId,
            String customerId,
            String customerName,
            String customerStatus,
            List<OrderLineDocument> lines,
            BigDecimal totalAmount,
            Instant processedAt,
            String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerStatus = customerStatus;
        this.lines = lines;
        this.totalAmount = totalAmount;
        this.processedAt = processedAt;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public List<OrderLineDocument> getLines() {
        return lines;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getStatus() {
        return status;
    }
}
