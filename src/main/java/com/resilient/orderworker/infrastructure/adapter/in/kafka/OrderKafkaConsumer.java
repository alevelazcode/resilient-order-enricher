/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.in.ProcessOrderUseCase;
import com.resilient.orderworker.application.port.out.FailedMessageStore;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

/**
 * Kafka inbound adapter. The listener thread blocks on the reactive pipeline so that the manual
 * acknowledgement only commits the offset after the message has been processed or routed into the
 * Redis-backed retry store, preserving per-partition ordering. Cross-partition parallelism comes
 * from the container concurrency setting.
 */
@Component
@Validated
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OrderKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(OrderKafkaConsumer.class);

    private final ProcessOrderUseCase processOrder;
    private final FailedMessageStore failedMessageStore;

    public OrderKafkaConsumer(
            ProcessOrderUseCase processOrder, FailedMessageStore failedMessageStore) {
        this.processOrder = processOrder;
        this.failedMessageStore = failedMessageStore;
    }

    @KafkaListener(
            topics = "${kafka.topics.orders:orders}",
            groupId = "${kafka.consumer.group-id:order-worker-group}")
    public void consume(
            @Payload @Valid OrderMessagePayload payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        LOG.info("Received order {} partition={} offset={}", payload.orderId(), partition, offset);
        ProcessOrderCommand command = payload.toCommand();

        try {
            processOrder
                    .process(command)
                    .doOnSuccess(order -> LOG.info("Processed order {}", order.orderId()))
                    .onErrorResume(
                            err -> {
                                LOG.warn("Order {} failed: {}", command.orderId(), err.toString());
                                return failedMessageStore.store(command, err).then(Mono.empty());
                            })
                    .block();
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
