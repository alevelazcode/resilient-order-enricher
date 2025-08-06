package com.resilient.orderworker.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the customer response from the Go API.
 */
public record CustomerResponse(
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("name")
    String name,
    
    @JsonProperty("status")
    String status
) {
    
    /**
     * Checks if the customer is active.
     * @return true if customer status is "ACTIVE"
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}