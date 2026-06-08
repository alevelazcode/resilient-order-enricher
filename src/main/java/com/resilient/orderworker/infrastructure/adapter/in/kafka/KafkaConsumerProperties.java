/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.kafka;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Typed binding for the worker's Kafka consumer configuration. */
@ConfigurationProperties(prefix = "kafka")
public record KafkaConsumerProperties(
        @NotBlank String bootstrapServers, Topics topics, Consumer consumer) {

    public KafkaConsumerProperties {
        topics = topics == null ? Topics.defaults() : topics;
        consumer = consumer == null ? Consumer.defaults() : consumer;
    }

    public record Topics(@NotBlank String orders) {

        public static Topics defaults() {
            return new Topics("orders");
        }
    }

    public record Consumer(
            @NotBlank String groupId,
            @NotBlank String autoOffsetReset,
            @Positive int maxPollRecords,
            @Positive int concurrency,
            Duration processingTimeout) {

        public static Consumer defaults() {
            return new Consumer("order-worker-group", "earliest", 10, 3, Duration.ofSeconds(30));
        }
    }
}
