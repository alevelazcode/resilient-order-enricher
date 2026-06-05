/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import com.resilient.orderworker.domain.product.Product;

import reactor.core.publisher.Mono;

public interface ProductProvider {

    Mono<Product> getProduct(String productId);
}
