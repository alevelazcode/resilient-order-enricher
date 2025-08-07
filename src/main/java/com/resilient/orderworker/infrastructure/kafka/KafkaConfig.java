/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.resilient.orderworker.order.dto.OrderMessage;

/**
 * Kafka configuration for consuming order messages with reactive support.
 *
 * <p>This configuration class sets up Kafka consumer infrastructure for the order processing
 * system. It provides:
 *
 * <ul>
 *   <li><strong>Consumer Factory</strong>: Configures Kafka consumer properties and deserializers
 *   <li><strong>Listener Container Factory</strong>: Sets up concurrent message processing
 *   <li><strong>Manual Acknowledgment</strong>: Enables manual offset management for better control
 *   <li><strong>JSON Deserialization</strong>: Handles OrderMessage deserialization from JSON
 *   <li><strong>Concurrent Processing</strong>: Supports multiple consumer threads
 * </ul>
 *
 * <p><strong>Key Features:</strong>
 *
 * <ul>
 *   <li>Conditional configuration based on {@code kafka.enabled} property
 *   <li>Configurable bootstrap servers, group ID, and offset reset strategy
 *   <li>Manual acknowledgment mode for better control over message processing
 *   <li>JSON deserialization with trusted packages configuration
 *   <li>Concurrent processing with configurable concurrency level
 * </ul>
 *
 * <p><strong>Configuration Properties:</strong>
 *
 * <ul>
 *   <li>{@code kafka.bootstrap-servers}: Kafka broker addresses (default: localhost:9092)
 *   <li>{@code kafka.consumer.group-id}: Consumer group ID (default: order-worker-group)
 *   <li>{@code kafka.consumer.auto-offset-reset}: Offset reset strategy (default: earliest)
 *   <li>{@code kafka.consumer.max-poll-records}: Max records per poll (default: 10)
 * </ul>
 *
 * <p><strong>Example Usage:</strong>
 *
 * <pre>{@code
 * @KafkaListener(topics = "orders", groupId = "order-worker-group")
 * public void handleOrderMessage(OrderMessage orderMessage, Acknowledgment ack) {
 *     // Process order message
 *     orderProcessingService.processOrder(orderMessage)
 *         .doOnSuccess(order -> ack.acknowledge())
 *         .subscribe();
 * }
 * }</pre>
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 * @see OrderMessage
 * @see org.springframework.kafka.annotation.KafkaListener
 * @see org.springframework.kafka.support.Acknowledgment
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    /** Kafka bootstrap servers configuration. */
    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /** Consumer group ID for message consumption. */
    @Value("${kafka.consumer.group-id:order-worker-group}")
    private String groupId;

    /** Auto offset reset strategy for consumer. */
    @Value("${kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    /** Maximum number of records to poll in a single request. */
    @Value("${kafka.consumer.max-poll-records:10}")
    private String maxPollRecords;

    /**
     * Creates and configures the Kafka consumer factory.
     *
     * <p>This method configures the consumer factory with the following settings:
     *
     * <ul>
     *   <li>Bootstrap servers for Kafka cluster connection
     *   <li>Consumer group ID for message partitioning
     *   <li>Auto offset reset strategy (earliest/latest)
     *   <li>Maximum poll records for batch processing
     *   <li>String key deserializer and JSON value deserializer
     *   <li>Manual acknowledgment mode for better control
     *   <li>Trusted packages for JSON deserialization
     * </ul>
     *
     * @return ConsumerFactory configured for OrderMessage consumption
     * @see OrderMessage
     * @see org.springframework.kafka.core.ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, OrderMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderMessage.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates and configures the Kafka listener container factory.
     *
     * <p>This method sets up the listener container factory with the following features:
     *
     * <ul>
     *   <li>Manual acknowledgment mode for better control over message processing
     *   <li>Single message listener (not batch) for individual message processing
     *   <li>Concurrent processing with configurable concurrency level (default: 3)
     *   <li>Integration with the configured consumer factory
     * </ul>
     *
     * <p>The factory enables concurrent message processing while maintaining manual acknowledgment
     * control for better error handling and retry logic.
     *
     * @return ConcurrentKafkaListenerContainerFactory configured for order processing
     * @see org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
     * @see org.springframework.kafka.listener.ContainerProperties.AckMode
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderMessage>
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Manual acknowledgement mode for better control
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Enable batch listener for better performance
        factory.setBatchListener(false);

        // Set concurrency level
        factory.setConcurrency(3);

        return factory;
    }
}
