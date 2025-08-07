/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.order.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.order.dto.OrderMessage;
import com.resilient.orderworker.order.service.OrderProcessingService;

import reactor.core.publisher.Mono;

/**
 * Kafka consumer for processing order messages. Implements manual acknowledgment for better error
 * handling.
 */
@Component
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    private final OrderProcessingService orderProcessingService;

    public OrderConsumer(OrderProcessingService orderProcessingService) {
        this.orderProcessingService = orderProcessingService;
    }

    /**
     * Consumes order messages from Kafka topic.
     *
     * @param orderMessage the order message
     * @param partition the partition number
     * @param offset the message offset
     * @param acknowledgment manual acknowledgment
     */
    @KafkaListener(
            topics = "${kafka.topics.orders:orders}",
            groupId = "${kafka.consumer.group-id:order-worker-group}")
    public void consumeOrder(
            @Payload OrderMessage orderMessage,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        logger.info(
                "Received order message: {} from partition: {}, offset: {}",
                orderMessage.orderId(),
                partition,
                offset);

        orderProcessingService
                .processOrder(orderMessage)
                .doOnSuccess(
                        order -> {
                            logger.info("Successfully processed order: {}", order.getOrderId());
                            acknowledgment.acknowledge();
                        })
                .doOnError(
                        error -> {
                            logger.error(
                                    "Error processing order {}: {}",
                                    orderMessage.orderId(),
                                    error.getMessage(),
                                    error);
                            // In production, you might want to send to DLQ instead of just
                            // acknowledging
                            acknowledgment.acknowledge();
                        })
                .onErrorResume(
                        error -> {
                            // Handle any unexpected errors
                            logger.error(
                                    "Unexpected error processing order {}: {}",
                                    orderMessage.orderId(),
                                    error.getMessage(),
                                    error);
                            return Mono.empty();
                        })
                .subscribe();
    }
}
