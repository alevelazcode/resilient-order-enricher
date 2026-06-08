/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@ConditionalOnProperty(name = "redisson.enabled", havingValue = "true", matchIfMissing = true)
public class RedisFailedMessageAdapter implements FailedMessageStore {

    private static final Logger LOG = LoggerFactory.getLogger(RedisFailedMessageAdapter.class);

    private static final String MESSAGE_PREFIX = "failed_messages:";
    private static final String ATTEMPTS_PREFIX = "failed_attempts:";
    private static final String NEXT_RETRY_PREFIX = "failed_next_retry:";
    private static final String FAILED_SET = "failed_messages_set";
    private static final String DEAD_LETTER_PREFIX = "dead_letter:";
    private static final String DEAD_LETTER_SET = "dead_letter_queue";

    private final RedissonClient redisson;
    private final ObjectMapper objectMapper;
    private final BackoffPolicy backoffPolicy;

    public RedisFailedMessageAdapter(
            RedissonClient redisson, ObjectMapper objectMapper, BackoffPolicy backoffPolicy) {
        this.redisson = redisson;
        this.objectMapper = objectMapper;
        this.backoffPolicy = backoffPolicy;
    }

    @Override
    public Mono<Void> store(ProcessOrderCommand command, Throwable error) {
        return Mono.fromRunnable(() -> persist(command, error))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Flux<FailedMessage> readyForRetry() {
        return Mono.fromCallable(this::collectReady)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> remove(String orderId) {
        return Mono.fromRunnable(() -> cleanup(orderId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Long> pendingCount() {
        return Mono.fromCallable(() -> (long) redisson.<String>getSet(FAILED_SET).size())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> deadLetterCount() {
        return Mono.fromCallable(() -> (long) redisson.<String>getSet(DEAD_LETTER_SET).size())
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void persist(ProcessOrderCommand command, Throwable error) {
        String orderId = command.orderId();
        // RAtomicLong guarantees an atomic read-modify-write so two concurrent
        // failures for the same orderId increment to distinct values.
        RAtomicLong attemptsCounter = redisson.getAtomicLong(ATTEMPTS_PREFIX + orderId);
        int attempts = (int) attemptsCounter.incrementAndGet();

        if (attempts > backoffPolicy.maxAttempts()) {
            LOG.error("Order {} exhausted retries; moving to dead letter", orderId);
            moveToDeadLetter(command, error, attempts);
            return;
        }

        Duration delay = backoffPolicy.nextDelay(attempts);
        long nextRetryEpoch = System.currentTimeMillis() + delay.toMillis();
        FailedMessage message = new FailedMessage(command, attempts, nextRetryEpoch);

        redisson.getBucket(MESSAGE_PREFIX + orderId).set(serialize(message, orderId));
        redisson.getBucket(NEXT_RETRY_PREFIX + orderId).set(nextRetryEpoch);
        redisson.<String>getSet(FAILED_SET).add(orderId);

        LOG.warn(
                "Stored failed message {} (attempt {}/{}, next retry in {}ms): {}",
                orderId,
                attempts,
                backoffPolicy.maxAttempts(),
                delay.toMillis(),
                error.toString());
    }

    private void moveToDeadLetter(ProcessOrderCommand command, Throwable error, int attempts) {
        String orderId = command.orderId();
        FailedMessage dlqMessage = new FailedMessage(command, attempts, 0L);
        redisson.getBucket(DEAD_LETTER_PREFIX + orderId).set(serialize(dlqMessage, orderId));
        redisson.<String>getSet(DEAD_LETTER_SET).add(orderId);
        cleanup(orderId);
        LOG.error("Order {} moved to dead letter: {}", orderId, error.toString());
    }

    private void cleanup(String orderId) {
        redisson.getBucket(MESSAGE_PREFIX + orderId).delete();
        redisson.getAtomicLong(ATTEMPTS_PREFIX + orderId).delete();
        redisson.getBucket(NEXT_RETRY_PREFIX + orderId).delete();
        redisson.<String>getSet(FAILED_SET).remove(orderId);
    }

    private List<FailedMessage> collectReady() {
        long now = System.currentTimeMillis();
        RSet<String> ids = redisson.getSet(FAILED_SET);
        return ids.stream()
                .filter(id -> isReady(id, now))
                .map(this::loadMessage)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean isReady(String orderId, long now) {
        Long next = (Long) redisson.getBucket(NEXT_RETRY_PREFIX + orderId).get();
        return next != null && now >= next;
    }

    private FailedMessage loadMessage(String orderId) {
        String json = (String) redisson.getBucket(MESSAGE_PREFIX + orderId).get();
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, FailedMessage.class);
        } catch (JsonProcessingException e) {
            LOG.error("Cannot deserialize failed message {}: {}", orderId, e.toString());
            return null;
        }
    }

    private String serialize(FailedMessage message, String orderId) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize failed message: " + orderId, e);
        }
    }
}
