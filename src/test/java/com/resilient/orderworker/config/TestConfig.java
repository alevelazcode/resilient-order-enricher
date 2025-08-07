/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.resilient.orderworker.infrastructure.redis.FailedMessageService;
import com.resilient.orderworker.order.dto.OrderMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Test configuration for providing mock beans when external dependencies are disabled.
 *
 * @author Alejandro Velazco
 * @version 1.0.0
 * @since 1.0.0
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provides a mock FailedMessageService for testing when Redis is disabled.
     *
     * @return mock FailedMessageService
     */
    @Bean
    @Primary
    public FailedMessageService mockFailedMessageService() {
        final FailedMessageService mock = Mockito.mock(FailedMessageService.class);

        // Configure mock behavior
        Mockito.when(
                        mock.storeFailedMessage(
                                Mockito.any(OrderMessage.class), Mockito.any(Throwable.class)))
                .thenReturn(Mono.empty());

        Mockito.when(mock.getMessagesReadyForRetry()).thenReturn(Flux.empty());

        Mockito.when(mock.removeFailedMessage(Mockito.anyString())).thenReturn(Mono.empty());

        Mockito.when(mock.getAttemptCount(Mockito.anyString())).thenReturn(Mono.just(0));

        return mock;
    }
}
