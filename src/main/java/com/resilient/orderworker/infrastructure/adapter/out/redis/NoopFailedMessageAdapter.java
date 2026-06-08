/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resilient.orderworker.application.port.out.FailedMessageStore;

@Configuration
@ConditionalOnProperty(name = "redisson.enabled", havingValue = "false")
public class NoopFailedMessageAdapter {

    @Bean
    public FailedMessageStore noopFailedMessageStore() {
        return new NoopFailedMessageStore();
    }
}
