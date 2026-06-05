/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.service;

import org.springframework.stereotype.Service;

import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.application.port.out.OrderRepository;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderQueryService implements QueryOrdersUseCase {

    private final OrderRepository orderRepository;

    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Mono<Order> findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public Flux<Order> findByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId);
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
            ordersFlux = orderRepository.findByStatusAndCustomerId(status, customerId, page, size);
            countMono = orderRepository.countByStatusAndCustomerId(status, customerId);
        } else if (status != null) {
            ordersFlux = orderRepository.findByStatus(status, page, size);
            countMono = orderRepository.countByStatus(status);
        } else if (customerId != null) {
            ordersFlux = orderRepository.findByCustomerId(customerId, page, size);
            countMono = orderRepository.countByCustomerId(customerId);
        } else {
            ordersFlux = orderRepository.findAll(page, size);
            countMono = orderRepository.count();
        }

        return Mono.zip(ordersFlux.collectList(), countMono)
                .map(t -> new Page<>(t.getT1(), page, size, t.getT2()));
    }
}
