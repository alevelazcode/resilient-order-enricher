/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.infrastructure.redis.FailedMessageService;
import com.resilient.orderworker.order.service.OrderProcessingService;

import reactor.core.publisher.Mono;

/**
 * Scheduled service for processing failed messages with exponential backoff retry logic.
 *
 * <p>This scheduler:
 *
 * <ul>
 *   <li><strong>Polls Failed Messages</strong>: Regularly checks Redis for messages ready for retry
 *   <li><strong>Respects Backoff Timing</strong>: Only processes messages that have passed their
 *       retry delay
 *   <li><strong>Handles Retry Logic</strong>: Reprocesses failed messages using the main processing
 *       service
 *   <li><strong>Manages Cleanup</strong>: Removes successfully processed messages from failed
 *       storage
 * </ul>
 *
 * <p>The scheduler runs every 30 seconds by default and can be configured via application
 * properties. It only operates when Redis is enabled and failed message tracking is active.
 *
 * <p><strong>Configuration Properties:</strong>
 *
 * <ul>
 *   <li>{@code app.scheduler.failed-messages.enabled}: Enable/disable the scheduler (default: true)
 *   <li>{@code app.scheduler.failed-messages.fixed-delay}: Delay between runs in milliseconds
 *       (default: 30000)
 * </ul>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see FailedMessageService
 * @see OrderProcessingService
 */
@Component
@ConditionalOnProperty(
        name = "app.scheduler.failed-messages.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class FailedMessageRetryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedMessageRetryScheduler.class);

    private final FailedMessageService failedMessageService;
    private final OrderProcessingService orderProcessingService;

    /**
     * Constructs a new FailedMessageRetryScheduler with required dependencies.
     *
     * @param failedMessageService service for managing failed messages in Redis
     * @param orderProcessingService service for processing order messages
     */
    public FailedMessageRetryScheduler(
            final FailedMessageService failedMessageService,
            final OrderProcessingService orderProcessingService) {
        this.failedMessageService = failedMessageService;
        this.orderProcessingService = orderProcessingService;
    }

    /**
     * Scheduled method that processes failed messages ready for retry.
     *
     * <p>This method:
     *
     * <ul>
     *   <li>Retrieves all failed messages that have passed their retry delay
     *   <li>Attempts to reprocess each message using the main processing service
     *   <li>Removes successfully processed messages from failed storage
     *   <li>Leaves failed messages in storage for future retry attempts
     * </ul>
     *
     * <p>The method runs with a fixed delay to ensure one execution completes before the next one
     * starts, preventing overlapping retry attempts.
     */
    @Scheduled(fixedDelayString = "${app.scheduler.failed-messages.fixed-delay:30000}")
    public void processFailedMessages() {
        LOGGER.debug("Starting failed message retry processing");

        failedMessageService
                .getMessagesReadyForRetry()
                .flatMap(
                        failedMessage -> {
                            final String orderId = failedMessage.getOrderMessage().orderId();
                            LOGGER.info(
                                    "Retrying failed message for order {} (attempt {})",
                                    orderId,
                                    failedMessage.getAttemptCount());

                            return orderProcessingService
                                    .processOrder(failedMessage.getOrderMessage())
                                    .doOnSuccess(
                                            order -> {
                                                LOGGER.info(
                                                        "Successfully reprocessed order {} after {}"
                                                                + " attempts",
                                                        orderId,
                                                        failedMessage.getAttemptCount());
                                                // Remove from failed messages storage on success
                                                failedMessageService
                                                        .removeFailedMessage(orderId)
                                                        .subscribe();
                                            })
                                    .doOnError(
                                            error -> {
                                                LOGGER.warn(
                                                        "Retry failed for order {} (attempt {}):"
                                                                + " {}",
                                                        orderId,
                                                        failedMessage.getAttemptCount(),
                                                        error.getMessage());
                                                // Store the failed attempt (will increment counter
                                                // and update next retry time)
                                                failedMessageService
                                                        .storeFailedMessage(
                                                                failedMessage.getOrderMessage(),
                                                                error)
                                                        .subscribe();
                                            })
                                    .onErrorResume(
                                            error -> {
                                                // Log error but don't propagate to prevent stopping
                                                // the stream
                                                LOGGER.error(
                                                        "Unexpected error during retry for order"
                                                                + " {}: {}",
                                                        orderId,
                                                        error.getMessage(),
                                                        error);
                                                failedMessageService
                                                        .storeFailedMessage(
                                                                failedMessage.getOrderMessage(),
                                                                error)
                                                        .subscribe();
                                                return Mono.empty();
                                            });
                        })
                .doOnComplete(() -> LOGGER.debug("Completed failed message retry processing"))
                .doOnError(
                        error ->
                                LOGGER.error(
                                        "Error during failed message processing: {}",
                                        error.getMessage(),
                                        error))
                .onErrorResume(
                        error -> {
                            // Log error but continue with next scheduled execution
                            LOGGER.error(
                                    "Failed message retry processing encountered error: {}",
                                    error.getMessage(),
                                    error);
                            return Mono.empty();
                        })
                .subscribe();
    }

    /**
     * Scheduled method for cleanup of old failed messages and statistics logging.
     *
     * <p>This method runs less frequently to:
     *
     * <ul>
     *   <li>Log statistics about failed message counts
     *   <li>Perform any necessary cleanup operations
     *   <li>Monitor the health of the retry mechanism
     * </ul>
     */
    @Scheduled(
            fixedDelayString = "${app.scheduler.failed-messages.cleanup-delay:300000}") // 5 minutes
    public void cleanupAndStats() {
        LOGGER.debug("Running failed message cleanup and statistics");

        // Log current failed message statistics
        // TODO: Implement statistics gathering from Redis
        // For now, just log that cleanup ran
        LOGGER.info("Failed message cleanup completed");
    }
}
