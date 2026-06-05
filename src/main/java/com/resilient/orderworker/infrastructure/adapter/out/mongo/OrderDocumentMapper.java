/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.infrastructure.adapter.out.mongo;

import java.util.List;

import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderLine;
import com.resilient.orderworker.domain.order.OrderStatus;

final class OrderDocumentMapper {

    private OrderDocumentMapper() {}

    static OrderDocument toDocument(Order order) {
        List<OrderLineDocument> lines =
                order.lines().stream()
                        .map(
                                line ->
                                        new OrderLineDocument(
                                                line.productId(),
                                                line.name(),
                                                line.description(),
                                                line.unitPrice(),
                                                line.quantity()))
                        .toList();
        return new OrderDocument(
                order.orderId(),
                order.customerId(),
                order.customerName(),
                order.customerStatus(),
                lines,
                order.totalAmount(),
                order.processedAt(),
                order.status().name());
    }

    static Order toDomain(OrderDocument document) {
        List<OrderLine> lines =
                document.getLines().stream()
                        .map(
                                d ->
                                        new OrderLine(
                                                d.getProductId(),
                                                d.getName(),
                                                d.getDescription(),
                                                d.getUnitPrice(),
                                                d.getQuantity()))
                        .toList();
        return new Order(
                document.getOrderId(),
                document.getCustomerId(),
                document.getCustomerName(),
                document.getCustomerStatus(),
                lines,
                document.getTotalAmount(),
                document.getProcessedAt(),
                OrderStatus.valueOf(document.getStatus()));
    }
}
