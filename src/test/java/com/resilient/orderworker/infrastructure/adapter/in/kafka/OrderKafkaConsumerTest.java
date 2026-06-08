/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.in.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.in.ProcessOrderUseCase;
import com.resilient.orderworker.application.port.out.FailedMessageStore;
import com.resilient.orderworker.domain.exception.OrderProcessingException;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderLine;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OrderKafkaConsumerTest {

    @Mock private ProcessOrderUseCase processOrder;
    @Mock private FailedMessageStore failedMessageStore;
    @Mock private Acknowledgment acknowledgment;

    private OrderKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderKafkaConsumer(processOrder, failedMessageStore, Duration.ofSeconds(5));
    }

    @Test
    void onSuccess_acksOnceAndDoesNotStore() {
        OrderMessagePayload payload = samplePayload("o1");
        when(processOrder.process(any(ProcessOrderCommand.class)))
                .thenReturn(Mono.just(sampleOrder()));

        consumer.consume(payload, 0, 0L, acknowledgment);

        verify(failedMessageStore, never()).store(any(), any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void onFailure_routesToStoreAndAcks() {
        OrderMessagePayload payload = samplePayload("o2");
        when(processOrder.process(any(ProcessOrderCommand.class)))
                .thenReturn(Mono.error(new OrderProcessingException("boom")));
        when(failedMessageStore.store(any(), any())).thenReturn(Mono.empty());

        consumer.consume(payload, 0, 1L, acknowledgment);

        verify(failedMessageStore, times(1))
                .store(any(ProcessOrderCommand.class), any(OrderProcessingException.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void onTimeout_routesToStoreAndAcks() {
        OrderMessagePayload payload = samplePayload("o3");
        when(processOrder.process(any(ProcessOrderCommand.class))).thenReturn(Mono.never());
        when(failedMessageStore.store(any(), any())).thenReturn(Mono.empty());

        OrderKafkaConsumer fastTimeout =
                new OrderKafkaConsumer(processOrder, failedMessageStore, Duration.ofMillis(50));
        fastTimeout.consume(payload, 0, 2L, acknowledgment);

        verify(failedMessageStore, times(1)).store(any(ProcessOrderCommand.class), any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void ackHappensEvenWhenStoreFails() {
        OrderMessagePayload payload = samplePayload("o4");
        when(processOrder.process(any(ProcessOrderCommand.class)))
                .thenReturn(Mono.error(new RuntimeException("upstream")));
        when(failedMessageStore.store(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("store down")));

        try {
            consumer.consume(payload, 0, 3L, acknowledgment);
        } catch (RuntimeException ignored) {
            // store throwing propagates out of block, but the finally must still ack
        }

        verify(failedMessageStore).store(any(), any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    private static OrderMessagePayload samplePayload(String orderId) {
        return new OrderMessagePayload(
                orderId, "c1", List.of(new OrderMessagePayload.LinePayload("p1", 1)));
    }

    private static Order sampleOrder() {
        return Order.create(
                "o1",
                "c1",
                "Alice",
                "ACTIVE",
                List.of(new OrderLine("p1", "L", "d", new BigDecimal("10"), 1)));
    }
}
