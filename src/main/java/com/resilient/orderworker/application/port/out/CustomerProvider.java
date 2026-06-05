/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.out;

import com.resilient.orderworker.domain.customer.Customer;

import reactor.core.publisher.Mono;

public interface CustomerProvider {

    Mono<Customer> getCustomer(String customerId);
}
