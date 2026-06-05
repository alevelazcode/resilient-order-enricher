/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.resilient.orderworker.application.port.in.ProcessOrderUseCase;
import com.resilient.orderworker.application.port.out.FailedMessageStore;

import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(
        name = "app.scheduler.failed-messages.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class FailedMessageRetryScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(FailedMessageRetryScheduler.class);

    private final FailedMessageStore failedMessageStore;
    private final ProcessOrderUseCase processOrder;

    public FailedMessageRetryScheduler(
            FailedMessageStore failedMessageStore, ProcessOrderUseCase processOrder) {
        this.failedMessageStore = failedMessageStore;
        this.processOrder = processOrder;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.failed-messages.fixed-delay:30000}")
    public void retryFailedMessages() {
        failedMessageStore
                .readyForRetry()
                .flatMap(this::retry)
                .doOnError(err -> LOG.error("Retry batch failed: {}", err.toString()))
                .onErrorResume(err -> Mono.empty())
                .subscribe();
    }

    private Mono<Void> retry(FailedMessageStore.FailedMessage failed) {
        String orderId = failed.command().orderId();
        return processOrder
                .process(failed.command())
                .flatMap(order -> failedMessageStore.remove(orderId))
                .doOnSuccess(v -> LOG.info("Retry succeeded for order {}", orderId))
                .onErrorResume(
                        err -> {
                            LOG.warn("Retry failed for order {}: {}", orderId, err.toString());
                            return failedMessageStore.store(failed.command(), err);
                        });
    }
}
