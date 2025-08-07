/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.resilient.orderworker.order.dto.OrderMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mock implementation of FailedMessageService for testing when Redis is disabled.
 *
 * <p>This implementation provides a no-op version of the FailedMessageService
 * that can be used during testing when Redis is not available or when Redis
 * functionality should be disabled.
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false")
public class MockFailedMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockFailedMessageService.class);

    /**
     * Mock implementation that logs the failure but does not store anything.
     *
     * @param orderMessage the failed order message
     * @param error the error that caused the failure
     * @return Mono that completes immediately
     */
    public Mono<Void> storeFailedMessage(final OrderMessage orderMessage, final Throwable error) {
        LOGGER.info("Mock: Would store failed message for order {} with error: {}", 
            orderMessage.orderId(), error.getMessage());
        return Mono.empty();
    }

    /**
     * Mock implementation that returns no messages ready for retry.
     *
     * @return empty Flux
     */
    public Flux<FailedMessageService.FailedMessage> getMessagesReadyForRetry() {
        LOGGER.debug("Mock: No messages ready for retry");
        return Flux.empty();
    }

    /**
     * Mock implementation that logs the removal but does not actually remove anything.
     *
     * @param orderId the order ID to remove
     * @return Mono that completes immediately
     */
    public Mono<Void> removeFailedMessage(final String orderId) {
        LOGGER.info("Mock: Would remove failed message for order {}", orderId);
        return Mono.empty();
    }

    /**
     * Mock implementation that returns 0 attempts.
     *
     * @param orderId the order ID
     * @return Mono containing 0
     */
    public Mono<Integer> getAttemptCount(final String orderId) {
        return Mono.just(0);
    }
}
