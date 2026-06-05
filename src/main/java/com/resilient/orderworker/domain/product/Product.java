/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.domain.product;

import java.math.BigDecimal;

public record Product(String productId, String name, String description, BigDecimal price) {

    public boolean isValid() {
        return productId != null
                && !productId.isBlank()
                && name != null
                && !name.isBlank()
                && price != null
                && price.signum() > 0;
    }
}
