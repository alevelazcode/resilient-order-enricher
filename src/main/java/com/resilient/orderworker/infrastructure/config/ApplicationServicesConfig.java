/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resilient.orderworker.application.port.in.ProcessOrderUseCase;
import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.application.port.out.CustomerProvider;
import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.application.port.out.OrderQueryRepository;
import com.resilient.orderworker.application.port.out.OrderRepository;
import com.resilient.orderworker.application.port.out.ProductProvider;
import com.resilient.orderworker.application.service.OrderProcessor;
import com.resilient.orderworker.application.service.OrderQueryService;
import com.resilient.orderworker.domain.order.OrderAssembler;
import com.resilient.orderworker.domain.order.OrderValidator;

/**
 * Composition root for the application + domain services. Keeps the application and domain packages
 * free of Spring annotations so they remain pure POJOs that can be unit-tested without spinning up
 * a context.
 */
@Configuration(proxyBeanMethods = false)
public class ApplicationServicesConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public OrderValidator orderValidator() {
        return new OrderValidator();
    }

    @Bean
    public OrderAssembler orderAssembler(Clock clock) {
        return new OrderAssembler(clock);
    }

    @Bean
    public ProcessOrderUseCase processOrderUseCase(
            OrderRepository orderRepository,
            CustomerProvider customerProvider,
            ProductProvider productProvider,
            DistributedLock distributedLock,
            OrderValidator validator,
            OrderAssembler assembler) {
        return new OrderProcessor(
                orderRepository,
                customerProvider,
                productProvider,
                distributedLock,
                validator,
                assembler);
    }

    @Bean
    public QueryOrdersUseCase queryOrdersUseCase(OrderQueryRepository orderQueryRepository) {
        return new OrderQueryService(orderQueryRepository);
    }
}
