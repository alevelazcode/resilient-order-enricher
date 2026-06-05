/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@ConditionalOnBean(RedissonClient.class)
public class RedisFailedMessageAdapter implements FailedMessageStore {

    private static final Logger LOG = LoggerFactory.getLogger(RedisFailedMessageAdapter.class);

    private static final String MESSAGE_PREFIX = "failed_messages:";
    private static final String ATTEMPTS_PREFIX = "failed_attempts:";
    private static final String NEXT_RETRY_PREFIX = "failed_next_retry:";
    private static final String FAILED_SET = "failed_messages_set";
    private static final String DEAD_LETTER_PREFIX = "dead_letter:";
    private static final String DEAD_LETTER_SET = "dead_letter_queue";

    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_DELAY_MS = Duration.ofSeconds(1).toMillis();
    private static final long MAX_DELAY_MS = Duration.ofMinutes(5).toMillis();

    private final RedissonClient redisson;
    private final ObjectMapper objectMapper;

    public RedisFailedMessageAdapter(RedissonClient redisson, ObjectMapper objectMapper) {
        this.redisson = redisson;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> store(ProcessOrderCommand command, Throwable error) {
        return Mono.fromRunnable(() -> persist(command, error))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private void persist(ProcessOrderCommand command, Throwable error) {
        String orderId = command.orderId();
        RBucket<Integer> attemptsBucket = redisson.getBucket(ATTEMPTS_PREFIX + orderId);
        Integer current = attemptsBucket.get();
        int attempts = (current == null ? 0 : current) + 1;

        if (attempts > MAX_ATTEMPTS) {
            LOG.error("Order {} exhausted retries; moving to dead letter", orderId);
            moveToDeadLetter(command, error, attempts);
            return;
        }

        long delay = Math.min((long) (INITIAL_DELAY_MS * Math.pow(2, attempts - 1)), MAX_DELAY_MS);
        long nextRetryEpoch = System.currentTimeMillis() + delay;

        try {
            String json =
                    objectMapper.writeValueAsString(
                            new Envelope(command, attempts, nextRetryEpoch));
            redisson.getBucket(MESSAGE_PREFIX + orderId).set(json);
            attemptsBucket.set(attempts);
            redisson.getBucket(NEXT_RETRY_PREFIX + orderId).set(nextRetryEpoch);
            RSet<String> set = redisson.getSet(FAILED_SET);
            set.add(orderId);
            LOG.warn(
                    "Stored failed message {} (attempt {}/{}, next retry in {}ms)",
                    orderId,
                    attempts,
                    MAX_ATTEMPTS,
                    delay);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize failed message: " + orderId, e);
        }
    }

    private void moveToDeadLetter(ProcessOrderCommand command, Throwable error, int attempts) {
        String orderId = command.orderId();
        try {
            String json = objectMapper.writeValueAsString(new Envelope(command, attempts, 0L));
            redisson.getBucket(DEAD_LETTER_PREFIX + orderId).set(json);
            redisson.<String>getSet(DEAD_LETTER_SET).add(orderId);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to write dead-letter for {}: {}", orderId, e.toString());
        }
        cleanup(orderId);
    }

    private void cleanup(String orderId) {
        redisson.getBucket(MESSAGE_PREFIX + orderId).delete();
        redisson.getBucket(ATTEMPTS_PREFIX + orderId).delete();
        redisson.getBucket(NEXT_RETRY_PREFIX + orderId).delete();
        redisson.<String>getSet(FAILED_SET).remove(orderId);
    }

    @Override
    public Flux<FailedMessage> readyForRetry() {
        return Mono.fromCallable(this::collectReady)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    private List<FailedMessage> collectReady() {
        long now = System.currentTimeMillis();
        RSet<String> ids = redisson.getSet(FAILED_SET);
        return ids.stream()
                .filter(id -> isReady(id, now))
                .map(this::loadEnvelope)
                .filter(Objects::nonNull)
                .map(
                        env ->
                                new FailedMessage(
                                        env.command, env.attemptCount, env.nextRetryEpochMillis))
                .toList();
    }

    private boolean isReady(String orderId, long now) {
        Long next = (Long) redisson.getBucket(NEXT_RETRY_PREFIX + orderId).get();
        return next != null && now >= next;
    }

    private Envelope loadEnvelope(String orderId) {
        String json = (String) redisson.getBucket(MESSAGE_PREFIX + orderId).get();
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Envelope.class);
        } catch (JsonProcessingException e) {
            LOG.error("Cannot deserialize failed message {}: {}", orderId, e.toString());
            return null;
        }
    }

    @Override
    public Mono<Void> remove(String orderId) {
        return Mono.fromRunnable(() -> cleanup(orderId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    record Envelope(ProcessOrderCommand command, int attemptCount, long nextRetryEpochMillis) {}
}
