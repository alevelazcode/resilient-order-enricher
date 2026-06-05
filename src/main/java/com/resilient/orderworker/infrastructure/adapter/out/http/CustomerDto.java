/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.resilient.orderworker.domain.customer.Customer;

record CustomerDto(
        @JsonProperty("customerId") String customerId,
        @JsonProperty("name") String name,
        @JsonProperty("status") String status) {

    Customer toDomain() {
        return new Customer(customerId, name, status);
    }
}
