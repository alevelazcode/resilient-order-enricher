/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import com.resilient.orderworker.order.dto.OrderMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for managing failed order messages with retry tracking.
 *
 * <p>This interface provides methods for:
 * <ul>
 *   <li>Storing failed messages with attempt tracking
 *   <li>Retrieving messages ready for retry
 *   <li>Managing attempt counters
 *   <li>Cleaning up successfully processed messages
 * </ul>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FailedMessageManager {

    /**
     * Stores a failed message with attempt tracking.
     *
     * @param orderMessage the failed order message
     * @param error the error that caused the failure
     * @return Mono that completes when the message is stored
     */
    Mono<Void> storeFailedMessage(OrderMessage orderMessage, Throwable error);

    /**
     * Retrieves all failed messages that are ready for retry.
     *
     * @return Flux of failed messages ready for retry processing
     */
    Flux<FailedMessageService.FailedMessage> getMessagesReadyForRetry();

    /**
     * Removes a successfully processed message from failed storage.
     *
     * @param orderId the order ID to remove
     * @return Mono that completes when the message is removed
     */
    Mono<Void> removeFailedMessage(String orderId);

    /**
     * Gets the current attempt count for an order.
     *
     * @param orderId the order ID
     * @return Mono containing the attempt count
     */
    Mono<Integer> getAttemptCount(String orderId);
}
