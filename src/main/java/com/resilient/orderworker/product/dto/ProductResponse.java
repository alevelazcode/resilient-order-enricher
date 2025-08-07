/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** DTO representing the product response from the Go API. */
public record ProductResponse(
        @JsonProperty("productId") String productId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("price") Double price) {

    /**
     * Validates if the product has all required fields.
     *
     * @return true if product is valid
     */
    public boolean isValid() {
        return productId != null
                && !productId.trim().isEmpty()
                && name != null
                && !name.trim().isEmpty()
                && price != null
                && price > 0;
    }
}
