/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.http;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.resilient.orderworker.domain.product.Product;

record ProductDto(
        @JsonProperty("productId") String productId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("price") BigDecimal price) {

    Product toDomain() {
        return new Product(productId, name, description, price);
    }
}
