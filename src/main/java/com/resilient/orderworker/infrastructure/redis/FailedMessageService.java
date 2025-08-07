/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.redis;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilient.orderworker.order.dto.OrderMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service for managing failed order messages in Redis with attempt tracking.
 *
 * <p>This service provides functionality to:
 *
 * <ul>
 *   <li><strong>Store Failed Messages</strong>: Persist failed order messages with metadata
 *   <li><strong>Track Attempt Counters</strong>: Maintain retry attempt counts per message
 *   <li><strong>Calculate Backoff Delays</strong>: Implement exponential backoff logic
 *   <li><strong>Retrieve Failed Messages</strong>: Get messages for retry processing
 *   <li><strong>Clean Up</strong>: Remove messages that exceed max retry attempts
 * </ul>
 *
 * <p>The service uses Redis Hash data structure to store:
 *
 * <ul>
 *   <li>{@code failed_messages:{orderId}} - Main message data
 *   <li>{@code failed_attempts:{orderId}} - Attempt counter
 *   <li>{@code failed_next_retry:{orderId}} - Next retry timestamp
 * </ul>
 *
 * <p><strong>Example Usage:</strong>
 *
 * <pre>{@code
 * // Store failed message
 * failedMessageService.storeFailedMessage(orderMessage, exception)
 *     .subscribe();
 *
 * // Get messages ready for retry
 * failedMessageService.getMessagesReadyForRetry()
 *     .doOnNext(this::retryProcessing)
 *     .subscribe();
 * }</pre>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see OrderMessage
 * @see FailedMessage
 */
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class FailedMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedMessageService.class);

    private static final String FAILED_MESSAGES_KEY_PREFIX = "failed_messages:";
    private static final String FAILED_ATTEMPTS_KEY_PREFIX = "failed_attempts:";
    private static final String FAILED_NEXT_RETRY_KEY_PREFIX = "failed_next_retry:";
    private static final String FAILED_MESSAGES_SET = "failed_messages_set";

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long INITIAL_DELAY_SECONDS = 1;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final long MAX_DELAY_SECONDS = 300; // 5 minutes

    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new FailedMessageService with required dependencies.
     *
     * @param redisTemplate Redis template for data operations
     * @param objectMapper JSON object mapper for serialization
     */
    public FailedMessageService(
            final RedisTemplate redisTemplate, final ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Stores a failed message in Redis with attempt tracking and exponential backoff.
     *
     * <p>This method:
     *
     * <ul>
     *   <li>Increments the attempt counter for the message
     *   <li>Calculates the next retry time using exponential backoff
     *   <li>Stores the message data and metadata in Redis
     *   <li>Adds the message ID to the failed messages set for efficient retrieval
     * </ul>
     *
     * @param orderMessage the failed order message
     * @param error the error that caused the failure
     * @return Mono that completes when the message is stored
     */
    public Mono<Void> storeFailedMessage(final OrderMessage orderMessage, final Throwable error) {
        return Mono.fromCallable(
                        () -> {
                            final String orderId = orderMessage.orderId();
                            final String messageKey = FAILED_MESSAGES_KEY_PREFIX + orderId;
                            final String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + orderId;
                            final String nextRetryKey = FAILED_NEXT_RETRY_KEY_PREFIX + orderId;

                            // Get current attempt count
                            final Integer currentAttempts =
                                    redisTemplate.get(attemptsKey, Integer.class);
                            final int attempts =
                                    (currentAttempts != null) ? currentAttempts + 1 : 1;

                            if (attempts > MAX_RETRY_ATTEMPTS) {
                                LOGGER.error(
                                        "Message {} exceeded max retry attempts ({}), moving to"
                                                + " dead letter",
                                        orderId,
                                        MAX_RETRY_ATTEMPTS);
                                moveToDeadLetter(orderMessage, error, attempts);
                                return null;
                            }

                            // Calculate next retry time with exponential backoff
                            final long delaySeconds = calculateBackoffDelay(attempts);
                            final long nextRetryTimestamp =
                                    System.currentTimeMillis() + (delaySeconds * 1000);

                            // Create failed message object
                            final FailedMessage failedMessage =
                                    new FailedMessage(
                                            orderMessage,
                                            error.getMessage(),
                                            attempts,
                                            LocalDateTime.now(ZoneId.systemDefault()),
                                            nextRetryTimestamp);

                            // Store in Redis
                            try {
                                final String messageJson =
                                        objectMapper.writeValueAsString(failedMessage);
                                redisTemplate.set(messageKey, messageJson);
                                redisTemplate.set(attemptsKey, attempts);
                                redisTemplate.set(nextRetryKey, nextRetryTimestamp);
                                redisTemplate.addToSet(FAILED_MESSAGES_SET, orderId);

                                LOGGER.warn(
                                        "Stored failed message for order {} (attempt {}/{}), next"
                                                + " retry in {}s",
                                        orderId,
                                        attempts,
                                        MAX_RETRY_ATTEMPTS,
                                        delaySeconds);

                            } catch (JsonProcessingException e) {
                                LOGGER.error(
                                        "Failed to serialize failed message for order {}: {}",
                                        orderId,
                                        e.getMessage());
                                throw new RuntimeException("Failed to store failed message", e);
                            }

                            return null;
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * Retrieves all failed messages that are ready for retry based on their next retry timestamp.
     *
     * @return Flux of failed messages ready for retry processing
     */
    public Flux<FailedMessage> getMessagesReadyForRetry() {
        return Mono.fromCallable(
                        () -> {
                            final long currentTime = System.currentTimeMillis();
                            final List<String> orderIds =
                                    redisTemplate.getSetMembers(FAILED_MESSAGES_SET);

                            return orderIds.stream()
                                    .filter(orderId -> isReadyForRetry(orderId, currentTime))
                                    .map(this::getFailedMessage)
                                    .filter(failedMessage -> failedMessage != null)
                                    .toList();
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    /**
     * Removes a successfully processed message from the failed messages storage.
     *
     * @param orderId the order ID to remove
     * @return Mono that completes when the message is removed
     */
    public Mono<Void> removeFailedMessage(final String orderId) {
        return Mono.fromCallable(
                        () -> {
                            redisTemplate.delete(FAILED_MESSAGES_KEY_PREFIX + orderId);
                            redisTemplate.delete(FAILED_ATTEMPTS_KEY_PREFIX + orderId);
                            redisTemplate.delete(FAILED_NEXT_RETRY_KEY_PREFIX + orderId);
                            redisTemplate.removeFromSet(FAILED_MESSAGES_SET, orderId);

                            LOGGER.info(
                                    "Removed successfully processed failed message for order {}",
                                    orderId);
                            return null;
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * Gets the current attempt count for an order.
     *
     * @param orderId the order ID
     * @return Mono containing the attempt count (0 if no attempts)
     */
    public Mono<Integer> getAttemptCount(final String orderId) {
        return Mono.fromCallable(
                        () -> {
                            final Integer attempts =
                                    redisTemplate.get(
                                            FAILED_ATTEMPTS_KEY_PREFIX + orderId, Integer.class);
                            return attempts != null ? attempts : 0;
                        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Calculates exponential backoff delay for retry attempts.
     *
     * @param attemptNumber the current attempt number (1-based)
     * @return delay in seconds
     */
    private long calculateBackoffDelay(final int attemptNumber) {
        final double delay =
                INITIAL_DELAY_SECONDS * Math.pow(BACKOFF_MULTIPLIER, attemptNumber - 1);
        return Math.min((long) delay, MAX_DELAY_SECONDS);
    }

    /**
     * Checks if a failed message is ready for retry based on its next retry timestamp.
     *
     * @param orderId the order ID
     * @param currentTime current timestamp in milliseconds
     * @return true if ready for retry
     */
    private boolean isReadyForRetry(final String orderId, final long currentTime) {
        final Long nextRetryTime =
                redisTemplate.get(FAILED_NEXT_RETRY_KEY_PREFIX + orderId, Long.class);
        return nextRetryTime != null && currentTime >= nextRetryTime;
    }

    /**
     * Retrieves a failed message from Redis.
     *
     * @param orderId the order ID
     * @return FailedMessage or null if not found or parsing fails
     */
    private FailedMessage getFailedMessage(final String orderId) {
        try {
            final String messageJson =
                    redisTemplate.get(FAILED_MESSAGES_KEY_PREFIX + orderId, String.class);
            if (messageJson != null) {
                return objectMapper.readValue(messageJson, FailedMessage.class);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error(
                    "Failed to deserialize failed message for order {}: {}",
                    orderId,
                    e.getMessage());
        }
        return null;
    }

    /**
     * Moves a message to dead letter queue after exceeding max retry attempts.
     *
     * @param orderMessage the failed message
     * @param error the final error
     * @param attempts total number of attempts made
     */
    private void moveToDeadLetter(
            final OrderMessage orderMessage, final Throwable error, final int attempts) {
        // TODO: Implement dead letter queue logic
        // For now, just log and clean up from failed messages
        final String orderId = orderMessage.orderId();

        LOGGER.error(
                "Moving message {} to dead letter queue after {} attempts. Final error: {}",
                orderId,
                attempts,
                error.getMessage());

        // Clean up from failed messages storage
        redisTemplate.delete(FAILED_MESSAGES_KEY_PREFIX + orderId);
        redisTemplate.delete(FAILED_ATTEMPTS_KEY_PREFIX + orderId);
        redisTemplate.delete(FAILED_NEXT_RETRY_KEY_PREFIX + orderId);
        redisTemplate.removeFromSet(FAILED_MESSAGES_SET, orderId);

        // Store in dead letter queue key for manual investigation
        final String deadLetterKey = "dead_letter:" + orderId;
        try {
            final FailedMessage deadLetterMessage =
                    new FailedMessage(
                            orderMessage,
                            error.getMessage(),
                            attempts,
                            LocalDateTime.now(ZoneId.systemDefault()),
                            0L // No next retry
                            );
            final String messageJson = objectMapper.writeValueAsString(deadLetterMessage);
            redisTemplate.set(deadLetterKey, messageJson);
            redisTemplate.addToSet("dead_letter_queue", orderId);
        } catch (JsonProcessingException e) {
            LOGGER.error(
                    "Failed to store dead letter message for order {}: {}",
                    orderId,
                    e.getMessage());
        }
    }

    /** Data class representing a failed message with retry metadata. */
    public static class FailedMessage {
        private OrderMessage orderMessage;
        private String errorMessage;
        private int attemptCount;
        private LocalDateTime failedAt;
        private long nextRetryTimestamp;

        // Default constructor for Jackson
        public FailedMessage() {}

        public FailedMessage(
                final OrderMessage orderMessage,
                final String errorMessage,
                final int attemptCount,
                final LocalDateTime failedAt,
                final long nextRetryTimestamp) {
            this.orderMessage = orderMessage;
            this.errorMessage = errorMessage;
            this.attemptCount = attemptCount;
            this.failedAt = failedAt;
            this.nextRetryTimestamp = nextRetryTimestamp;
        }

        // Getters and setters
        public OrderMessage getOrderMessage() {
            return orderMessage;
        }

        public void setOrderMessage(final OrderMessage orderMessage) {
            this.orderMessage = orderMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getAttemptCount() {
            return attemptCount;
        }

        public void setAttemptCount(final int attemptCount) {
            this.attemptCount = attemptCount;
        }

        public LocalDateTime getFailedAt() {
            return failedAt;
        }

        public void setFailedAt(final LocalDateTime failedAt) {
            this.failedAt = failedAt;
        }

        public long getNextRetryTimestamp() {
            return nextRetryTimestamp;
        }

        public void setNextRetryTimestamp(final long nextRetryTimestamp) {
            this.nextRetryTimestamp = nextRetryTimestamp;
        }
    }
}
