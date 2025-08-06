package com.resilient.orderworker.common.exception;

/**
 * Exception thrown when external API calls fail.
 */
public class ExternalApiException extends RuntimeException {
    
    public ExternalApiException(String message) {
        super(message);
    }
    
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}