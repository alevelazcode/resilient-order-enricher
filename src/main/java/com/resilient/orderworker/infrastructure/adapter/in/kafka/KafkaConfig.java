/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfig.class);

    private final KafkaConsumerProperties properties;

    public KafkaConfig(KafkaConsumerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ConsumerFactory<String, OrderMessagePayload> orderConsumerFactory() {
        KafkaConsumerProperties.Consumer c = properties.consumer();
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, c.groupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, c.autoOffsetReset());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, c.maxPollRecords());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Wrap deserializers with ErrorHandlingDeserializer so a poisoned record
        // (bad JSON, wrong schema) is captured as a DeserializationException
        // routed to the error handler instead of stopping the container.
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(
                ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class.getName());
        props.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class.getName());

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.resilient.orderworker.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderMessagePayload.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderMessagePayload>
            kafkaListenerContainerFactory(
                    ConsumerFactory<String, OrderMessagePayload> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, OrderMessagePayload> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(properties.consumer().concurrency());

        // Drop poison-pill records after a single attempt; the listener itself
        // routes business failures into Redis for backoff-driven replay.
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        (record, exception) ->
                                LOG.error(
                                        "Dropping unrecoverable record (topic={}, partition={}, offset={}): {}",
                                        record.topic(),
                                        record.partition(),
                                        record.offset(),
                                        exception.toString()),
                        new FixedBackOff(0L, 0L));
        factory.setCommonErrorHandler(errorHandler);

        // Enable Micrometer observation for the container so polls and processing
        // surface in /actuator/prometheus and tracing exporters automatically.
        factory.getContainerProperties().setObservationEnabled(true);

        return factory;
    }
}
