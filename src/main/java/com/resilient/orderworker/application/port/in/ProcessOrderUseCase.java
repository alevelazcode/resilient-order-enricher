/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.in;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.domain.order.Order;

import reactor.core.publisher.Mono;

public interface ProcessOrderUseCase {

    Mono<Order> process(ProcessOrderCommand command);
}
