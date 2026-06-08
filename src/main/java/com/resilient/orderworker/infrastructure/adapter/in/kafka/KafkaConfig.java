/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

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

    /**
     * Producer used by {@link DeadLetterPublishingRecoverer} to forward poison-pill records to the
     * DLQ topic. Idempotent producer + {@code acks=all} so a single record is never duplicated and
     * never silently lost. Raw bytes (no schema) so the rejected payload reaches the DLQ exactly as
     * it arrived, ready for inspection or replay.
     */
    @Bean
    public ProducerFactory<Object, Object> dlqProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<Object, Object> dlqKafkaTemplate(
            ProducerFactory<Object, Object> dlqProducerFactory) {
        return new KafkaTemplate<>(dlqProducerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderMessagePayload>
            kafkaListenerContainerFactory(
                    ConsumerFactory<String, OrderMessagePayload> consumerFactory,
                    KafkaTemplate<Object, Object> dlqKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, OrderMessagePayload> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(properties.consumer().concurrency());

        // Poison pills (deserialisation failures) are forwarded to the DLQ topic on the same
        // partition, so an operator can replay them with standard kafka-console-consumer tooling.
        // Business failures inside the listener continue to flow through the Redis retry store.
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        dlqKafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(properties.topics().dlq(), record.partition()));
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 0L));
        factory.setCommonErrorHandler(errorHandler);

        // Enable Micrometer observation for the container so polls and processing
        // surface in /actuator/prometheus and tracing exporters automatically.
        factory.getContainerProperties().setObservationEnabled(true);

        return factory;
    }
}
