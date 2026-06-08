/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import com.resilient.orderworker.application.port.in.ProcessOrderUseCase;
import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.application.port.out.FailedMessageStore;
import com.resilient.orderworker.infrastructure.adapter.in.rest.OrderController;

/**
 * Smoke test that boots the full Spring context with the test profile. It does not exercise
 * behaviour — its only job is to fail fast on wiring regressions: missing beans, ambiguous
 * dependencies, broken @ConditionalOnProperty chains, or autoconfigure mismatches.
 *
 * <p>External infrastructure (Kafka, Mongo, Redis) is excluded by the test profile; the Noop
 * fallbacks satisfy {@link DistributedLock} and {@link FailedMessageStore}.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextSmokeTest {

    @Autowired private ApplicationContext context;
    @Autowired private ProcessOrderUseCase processOrder;
    @Autowired private QueryOrdersUseCase queryOrders;
    @Autowired private DistributedLock distributedLock;
    @Autowired private FailedMessageStore failedMessageStore;
    @Autowired private OrderController orderController;

    @Test
    void contextLoadsWithAllPortsAndAdaptersWired() {
        assertThat(context).isNotNull();
        assertThat(processOrder).isNotNull();
        assertThat(queryOrders).isNotNull();
        assertThat(distributedLock).isNotNull();
        assertThat(failedMessageStore).isNotNull();
        assertThat(orderController).isNotNull();
    }
}
