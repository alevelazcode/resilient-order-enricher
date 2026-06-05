/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Field;

public class OrderLineDocument {

    @Field("productId")
    private String productId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("unitPrice")
    private BigDecimal unitPrice;

    @Field("quantity")
    private int quantity;

    public OrderLineDocument() {}

    public OrderLineDocument(
            String productId, String name, String description, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
