package com.resilient.orderworker.common.exception;

/**
 * Exception thrown when order processing fails.
 */
public class OrderProcessingException extends RuntimeException {
    
    public OrderProcessingException(String message) {
        super(message);
    }
    
    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}