/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.port.in;

import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QueryOrdersUseCase {

    Mono<Order> findByOrderId(String orderId);

    Flux<Order> findByCustomerId(String customerId);

    Mono<Page<Order>> findAll(PageQuery query);

    record PageQuery(int page, int size, OrderStatus status, String customerId) {

        public PageQuery {
            if (page < 0) {
                throw new IllegalArgumentException("page must be >= 0");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("size must be between 1 and 100");
            }
        }
    }

    record Page<T>(java.util.List<T> content, int page, int size, long totalElements) {

        public int totalPages() {
            return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        }

        public boolean first() {
            return page == 0;
        }

        public boolean last() {
            return page >= totalPages() - 1;
        }
    }
}
