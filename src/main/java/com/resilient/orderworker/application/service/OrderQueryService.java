/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.service;

import org.springframework.stereotype.Service;

import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.application.port.out.OrderQueryRepository;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderQueryService implements QueryOrdersUseCase {

    private final OrderQueryRepository orderQueryRepository;

    public OrderQueryService(OrderQueryRepository orderQueryRepository) {
        this.orderQueryRepository = orderQueryRepository;
    }

    @Override
    public Mono<Order> findByOrderId(String orderId) {
        return orderQueryRepository.findByOrderId(orderId);
    }

    @Override
    public Flux<Order> findByCustomerId(String customerId) {
        return orderQueryRepository.findByCustomerId(customerId);
    }

    @Override
    public Mono<Page<Order>> findAll(PageQuery query) {
        OrderStatus status = query.status();
        String customerId = query.customerId();
        int page = query.page();
        int size = query.size();

        Flux<Order> ordersFlux;
        Mono<Long> countMono;

        if (status != null && customerId != null) {
            ordersFlux =
                    orderQueryRepository.findByStatusAndCustomerId(status, customerId, page, size);
            countMono = orderQueryRepository.countByStatusAndCustomerId(status, customerId);
        } else if (status != null) {
            ordersFlux = orderQueryRepository.findByStatus(status, page, size);
            countMono = orderQueryRepository.countByStatus(status);
        } else if (customerId != null) {
            ordersFlux = orderQueryRepository.findByCustomerId(customerId, page, size);
            countMono = orderQueryRepository.countByCustomerId(customerId);
        } else {
            ordersFlux = orderQueryRepository.findAll(page, size);
            countMono = orderQueryRepository.count();
        }

        return Mono.zip(ordersFlux.collectList(), countMono)
                .map(t -> new Page<>(t.getT1(), page, size, t.getT2()));
    }
}
