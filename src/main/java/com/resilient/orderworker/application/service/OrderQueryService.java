/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.service;

import com.resilient.orderworker.application.port.in.QueryOrdersUseCase;
import com.resilient.orderworker.application.port.out.OrderQueryRepository;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Read-side use case. Pure POJO — wired by {@link
 * com.resilient.orderworker.infrastructure.config.ApplicationServicesConfig}.
 */
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
    public Mono<Page<Order>> findAll(PageQuery query) {
        int page = query.page();
        int size = query.size();
        OrdersAndCount queryResult = resolveQuery(query);

        return Mono.zip(queryResult.orders().collectList(), queryResult.total())
                .map(t -> new Page<>(t.getT1(), page, size, t.getT2()));
    }

    private OrdersAndCount resolveQuery(PageQuery query) {
        OrderStatus status = query.status();
        String customerId = query.customerId();
        int page = query.page();
        int size = query.size();

        if (status != null && customerId != null) {
            return new OrdersAndCount(
                    orderQueryRepository.findByStatusAndCustomerId(status, customerId, page, size),
                    orderQueryRepository.countByStatusAndCustomerId(status, customerId));
        }
        if (status != null) {
            return new OrdersAndCount(
                    orderQueryRepository.findByStatus(status, page, size),
                    orderQueryRepository.countByStatus(status));
        }
        if (customerId != null) {
            return new OrdersAndCount(
                    orderQueryRepository.findByCustomerId(customerId, page, size),
                    orderQueryRepository.countByCustomerId(customerId));
        }
        return new OrdersAndCount(
                orderQueryRepository.findAll(page, size), orderQueryRepository.count());
    }

    private record OrdersAndCount(Flux<Order> orders, Mono<Long> total) {}
}
